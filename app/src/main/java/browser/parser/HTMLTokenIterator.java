package browser.parser;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import browser.model.HTMLToken;

public class HTMLTokenIterator implements ListIterator<HTMLToken> {

    private final List<HTMLToken> tokens;

    private int index;

    public HTMLTokenIterator(List<HTMLToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean hasNext() {
        return index < tokens.size() - 1;
    }

    @Override
    public HTMLToken next() {
        if (hasNext()) {
            return tokens.get(index++);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public HTMLToken previous() {
        if (hasPrevious()) {
            return tokens.get(index--);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int nextIndex() {
        return index + 1;
    }

    @Override
    public int previousIndex() {
        return index - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(HTMLToken htmlToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(HTMLToken htmlToken) {
        throw new UnsupportedOperationException();
    }
}
