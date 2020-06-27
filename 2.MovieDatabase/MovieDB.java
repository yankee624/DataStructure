
/**
 * Genre, Title 을 관리하는 영화 데이터베이스.
 * 
 * MyLinkedList 를 사용해 각각 Genre와 Title에 따라 내부적으로 정렬된 상태를  
 * 유지하는 데이터베이스이다. 
 */
public class MovieDB {
    private MyLinkedList<GenreItem> GenreList;
	
	public MovieDB() {
		GenreList = new MyLinkedList<GenreItem>();
    }
	
    public void insert(MovieDBItem item) {
    	GenreItem targetGenreItem = null;
    	String targetGenre = item.getGenre();
    	
    	///// 1. DOES ITEM'S GENRE EXIST IN GenreList? /////
    	MyLinkedListIterator<GenreItem> genIt = GenreList.iterator();
    	boolean found = false;
    	boolean inserted = false;
    	while(genIt.hasNext()) {
    		GenreItem currentGenreItem = genIt.next();
    		String name = currentGenreItem.getGenre();
    		// item's genre exists
    		if(name.compareTo(targetGenre) == 0) {
    			targetGenreItem = currentGenreItem;
    			found = true;
    			break;
    		}
    		// item's genre doesn't exist -> insert in the right place
    		else if(name.compareTo(targetGenre) > 0) {
    			targetGenreItem = new GenreItem(item);
    			genIt.add(targetGenreItem);
    			inserted = true;
    			break;
    		}
    	}
    	
    	if(!found && !inserted) {
    		// item's genre is larger than all other genres -> insert at the end 
    		targetGenreItem = new GenreItem(item);
    		GenreList.add(targetGenreItem);
    	} 
    	
    	///// 2. DOES THE ITEM EXIST IN MovieList of that GenreList? /////
    	else if(found) {
    		MyLinkedList<MovieDBItem> movieList = targetGenreItem.getMovieList();
    		MyLinkedListIterator<MovieDBItem> movIt = movieList.iterator();
    		found = false;
    		inserted = false;
    		while(movIt.hasNext()) {
    			MovieDBItem currentMovieDBItem = movIt.next();
    			// item exists
    			if(currentMovieDBItem.compareTo(item) == 0) {
    				found = true;
    				break;
    			}
    			// item doesn't exist -> insert in the right place
    			else if(currentMovieDBItem.compareTo(item) > 0) {
    				movIt.add(item);
    				inserted = true;
    				break;
    			}
    		}
    		// item's movie name is larger than all other movies -> insert at the end
    		if(!found && !inserted) {
    			movieList.add(item);
    		}
    	}
    	
    }

    public void delete(MovieDBItem item) {
    	GenreItem targetGenreItem = null;
    	String targetGenre = item.getGenre();
    	MyLinkedListIterator<GenreItem> genIt = GenreList.iterator();
    	boolean found = false;
    	boolean deleteGenre = false;

    	///// 1. DOES ITEM'S GENRE EXIST IN GenreList? /////
    	while(genIt.hasNext()) {
    		GenreItem currentGenreItem = genIt.next();
    		String name = currentGenreItem.getGenre();
    		// item's genre exists
    		if(name.compareTo(targetGenre) == 0) {
    			targetGenreItem = currentGenreItem;
    			// given item is the only item in that genre -> delete genre
    			if(targetGenreItem.getMovieList().size() == 1 && targetGenreItem.getMovieList().first().equals(item)) {
    				genIt.remove();
    				deleteGenre = true;
    			}
    			found = true;
    			break;
    		}
    		// item's genre doesn't exist -> do nothing
    		else if(name.compareTo(targetGenre) > 0) {
    			targetGenreItem = new GenreItem(item);
    			break;
    		}
    	}
    	
    	///// 2. DOES THE ITEM EXIST IN MovieList of that GenreList? /////
    	if(found && !deleteGenre) {
    		MyLinkedList<MovieDBItem> movieList = targetGenreItem.getMovieList();
    		MyLinkedListIterator<MovieDBItem> movIt = movieList.iterator();
    		while(movIt.hasNext()) {
    			MovieDBItem currentMovieDBItem = movIt.next();
    			// item exists
    			if(currentMovieDBItem.compareTo(item) == 0) {
    				movIt.remove();
    				break;
    			}
    			// item doesn't exist -> do nothing
    			else if(currentMovieDBItem.compareTo(item) > 0) {
    				break;
    			}
    		}
    	}

    }

    public MyLinkedList<GenreItem> getGenreList(){
    	return GenreList;
    }
    
}

class GenreItem implements Comparable<GenreItem> {
	private String genre;
	private MyLinkedList<MovieDBItem> movieList;
	
	public GenreItem(String name) {
		genre = name;
		movieList = new MyLinkedList<MovieDBItem>();
	}
	
	public GenreItem(MovieDBItem item) {
		genre = item.getGenre();
		movieList = new MyLinkedList<MovieDBItem>();
		movieList.add(item);
	}
	
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public MyLinkedList<MovieDBItem> getMovieList() {
		return movieList;
	}

	public void setMovieList(MyLinkedList<MovieDBItem> movieList) {
		this.movieList = movieList;
	}
	
	@Override
	public int compareTo(GenreItem o) {
		return this.getGenre().compareTo(o.getGenre());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genre == null) ? 0 : genre.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GenreItem))
			return false;
		GenreItem other = (GenreItem) obj;
		if (genre == null) {
			if (other.genre != null)
				return false;
		} else if (!genre.equals(other.genre))
			return false;
		return true;
	}
}
