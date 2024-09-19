package business.order;

import api.ApiException;
import business.BookstoreDbException;
import business.JdbcUtils;
import business.book.Book;
import business.book.BookDao;
import business.cart.ShoppingCart;
import business.cart.ShoppingCartItem;
import business.customer.Customer;
import business.customer.CustomerDao;
import business.customer.CustomerForm;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class  DefaultOrderService implements OrderService {

	private BookDao bookDao;

	public void setBookDao(BookDao bookDao) {
		this.bookDao = bookDao;
	}

	private OrderDao orderDao;

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}

	private LineItemDao lineItemDao;

	public void setLineItemDao(LineItemDao lineItemDao) {
		this.lineItemDao = lineItemDao;
	}

	private CustomerDao customerDao;

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}
	@Override
	public OrderDetails getOrderDetails(long orderId) {
		Order order = orderDao.findByOrderId(orderId);
		Customer customer = customerDao.findByCustomerId(order.getCustomerId());
		List<LineItem> lineItems = lineItemDao.findByOrderId(orderId);
		List<Book> books = lineItems
				.stream()
				.map(lineItem -> bookDao.findByBookId(lineItem.getBookId()))
				.collect(Collectors.toList());
		return new OrderDetails(order, customer, lineItems, books);
	}

	private Date getDate(String monthString, String yearString) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
		String dateString = monthString + '/' + yearString;
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
    public long placeOrder(CustomerForm customerForm, ShoppingCart cart) {

		validateCustomer(customerForm);
		validateCart(cart);

		// NOTE: MORE CODE PROVIDED NEXT PROJECT

		try (Connection connection = JdbcUtils.getConnection()) {
			Date date = getDate(
					customerForm.getCcExpiryMonth(),
					customerForm.getCcExpiryYear());
			return performPlaceOrderTransaction(
					customerForm.getName(),
					customerForm.getAddress(),
					customerForm.getPhone(),
					customerForm.getEmail(),
					customerForm.getCcNumber(),
					date, cart, connection);
		} catch (SQLException e) {
			throw new BookstoreDbException("Error during close connection for customer order", e);
		}
	}


	private void validateCustomer(CustomerForm customerForm) {

    	String name = customerForm.getName();

		if (name == null || name.equals("") || name.length() > 45 || name.length() < 4) {
			throw new ApiException.ValidationFailure("name","Invalid name field");
		}

		String address = customerForm.getAddress();
		if (address == null || address.equals("") || address.length() > 45  || address.length() < 4) {
			throw new ApiException.ValidationFailure("address","Invalid address field");
		}

		String phone = customerForm.getPhone();
		if(phone == null || phone.equals("") || phone.replaceAll("\\D","").length() != 10) {
			throw new ApiException.ValidationFailure("phone","Invalid phone field");
		}

		String email = customerForm.getEmail();
		if (email == null || email.equals("") || !email.matches("[^\\s]+@[\\w]+\\.[\\w]+[^.]")) {
			throw new ApiException.ValidationFailure("email","Invalid email field");
		}

		String creditCardNumber = customerForm.getCcNumber();
		if (creditCardNumber == null || creditCardNumber.equals("") || !creditCardNumber.replaceAll("[\\s-]+", "").matches("^[0-9]{14,16}$")) {
			throw new ApiException.ValidationFailure("ccNumber","Invalid ccNumber field");
		}



		if (!expiryDateIsInvalid(customerForm.getCcExpiryMonth(), customerForm.getCcExpiryYear())) {
			throw new ApiException.ValidationFailure("Please enter a valid expiration date.");

		}
	}

	private boolean expiryDateIsInvalid(String ccExpiryMonth, String ccExpiryYear) {


		try {
			int currentMonth = LocalDate.now().getMonthValue();
			int currentYear = LocalDate.now().getYear();
			int expirationMonth = Integer.parseInt(ccExpiryMonth);
			int expirationYear = Integer.parseInt(ccExpiryYear);

			if (expirationYear > currentYear || (expirationYear == currentYear && expirationMonth >= currentMonth)) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}

	}

	private void validateCart(ShoppingCart cart) {

		if (cart.getItems().size() <= 0) {
			throw new ApiException.ValidationFailure("Cart is empty.");
		}

		cart.getItems().forEach(item-> {
			if (item.getQuantity() < 0 || item.getQuantity() > 99) {
				throw new ApiException.ValidationFailure("Invalid quantity");
			}
			Book databaseBook = bookDao.findByBookId(item.getBookId());

			if(databaseBook.getPrice() != item.getBookForm().getPrice()) {
				throw new ApiException.ValidationFailure("Invalid price");
			}
			if(databaseBook.getCategoryId() != item.getBookForm().getCategoryId()) {
				throw new ApiException.ValidationFailure("Invalid category");
			}
		});
	}

	private int generateConfirmationNumber() {
		Random random = new Random();
		return random.nextInt(900000000) + 100000000; // TODO Implement this correctly
	}
	private long performPlaceOrderTransaction(
			String name, String address, String phone,
			String email, String ccNumber, Date date,
			ShoppingCart cart, Connection connection) {
		try {
			connection.setAutoCommit(false);
			long customerId = customerDao.create(
					connection, name, address, phone, email,
					ccNumber, date);
			long customerOrderId = orderDao.create(
					connection,
					cart.getComputedSubtotal() + cart.getSurcharge(),
					generateConfirmationNumber(), customerId);
			for (ShoppingCartItem item : cart.getItems()) {
				lineItemDao.create(connection, customerOrderId,
						item.getBookId(), item.getQuantity());
			}
			connection.commit();
			return customerOrderId;
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new BookstoreDbException("Failed to roll back transaction", e1);
			}
			return 0;
		}
	}

}
