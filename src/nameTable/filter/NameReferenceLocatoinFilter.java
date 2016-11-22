package nameTable.filter;

import nameTable.nameReference.NameReference;
import sourceCodeAST.SourceCodeLocation;

/**
 * The filter accepts name references between two locations, greater than or equal to start (if it is not null) and less than end (if 
 * it is not null) 
 * @author Zhou Xiaocong
 * @since 2016Äê11ÔÂ13ÈÕ
 * @version 1.0
 *
 */
public class NameReferenceLocatoinFilter extends NameTableFilter {

	private NameTableFilter wrappedFilter = null;
	private SourceCodeLocation start = null;
	private SourceCodeLocation end = null;
	
	public NameReferenceLocatoinFilter(NameTableFilter wrappedFilter) {
		this.wrappedFilter = wrappedFilter;
	}

	public NameReferenceLocatoinFilter(SourceCodeLocation start) {
		this.start = start;
	}

	public NameReferenceLocatoinFilter(SourceCodeLocation start, SourceCodeLocation end) {
		this.start = start;
		this.end = end;
	}

	public NameReferenceLocatoinFilter(NameTableFilter wrappedFilter, SourceCodeLocation start, SourceCodeLocation end) {
		this.wrappedFilter = wrappedFilter;
		this.start = start;
		this.end = end;
	}

	public NameReferenceLocatoinFilter(NameTableFilter wrappedFilter, SourceCodeLocation start) {
		this.wrappedFilter = wrappedFilter;
		this.start = start;
	}

	public void setAcceptableLocation(SourceCodeLocation start, SourceCodeLocation end) {
		this.start = start;
		this.end = end;
	}
	
	public void setAcceptableStartLocation(SourceCodeLocation start) {
		this.start = start;
	}

	public void setAcceptableEndLocation(SourceCodeLocation end) {
		this.end = end;
	}

	@Override
	public boolean accept(NameReference reference) {
		boolean acceptable = true;
		if (wrappedFilter != null) acceptable = wrappedFilter.accept(reference);
		
		if (acceptable && start != null) {
			acceptable = (start.compareTo(reference.getLocation()) <= 0);
		}

		if (acceptable && end != null) {
			acceptable = (end.compareTo(reference.getLocation()) > 0);
		}
		return acceptable;
	}
}
