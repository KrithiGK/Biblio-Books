package business.book;

public class Book {

	/*
	 * TODO: Create private fields corresponding to the fields in the
	 * book table of your database. Generate a constructor that
	 * uses those fields. Generate getter methods for those fields,
	 * and generate a toString method that uses those fields.
	 */

	private final long categoryId;
	private final long bookId;
	private final String title;
	private final String author;
	private final String description;
	private final int price;
	private final boolean isPublic;
	private final int rating;
	private final boolean isFeatured;

	public long getCategoryId() {
		return categoryId;
	}

	public long getBookId() {
		return bookId;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public int getRating() {
		return rating;
	}

	public boolean getIsFeatured() {
		return isFeatured;
	}

	public Book(long categoryId, long bookId, String title, String author, String description, int price, boolean isPublic, int rating, boolean isFeatured) {
		this.categoryId = categoryId;
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.description = description;
		this.price = price;
		this.isPublic = isPublic;
		this.rating = rating;
		this.isFeatured = isFeatured;
	}
}
