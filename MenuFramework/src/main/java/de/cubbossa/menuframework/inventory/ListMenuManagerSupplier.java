package de.cubbossa.menuframework.inventory;

public interface ListMenuManagerSupplier<T> extends ListMenuSupplier<T> {

    boolean deleteFromMenu(T object);

    boolean duplicateElementFromMenu(T object);

    boolean newElementFromMenu(Object[] args);
}
