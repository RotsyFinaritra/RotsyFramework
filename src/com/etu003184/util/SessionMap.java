package com.etu003184.util;


import jakarta.servlet.http.HttpSession;
import java.util.*;

public class SessionMap extends AbstractMap<String, Object> {
    private final HttpSession session;

    public SessionMap(HttpSession session) {
        this.session = session;
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        Object old = session.getAttribute(key);
        session.setAttribute(key, value);
        return old;
    }

    @Override
    public Object remove(Object key) {
        Object old = session.getAttribute((String) key);
        session.removeAttribute((String) key);
        return old;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        // Return a live view backed by the HttpSession attributes.
        return new AbstractSet<Entry<String, Object>>() {
            @Override
            public Iterator<Entry<String, Object>> iterator() {
                Enumeration<String> names = session.getAttributeNames();
                return new Iterator<Entry<String, Object>>() {
                    String last = null;
                    String next = names.hasMoreElements() ? names.nextElement() : null;

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public Entry<String, Object> next() {
                        if (next == null) throw new NoSuchElementException();
                        last = next;
                        String current = next;
                        next = names.hasMoreElements() ? names.nextElement() : null;
                        return new SessionEntry(current);
                    }

                    @Override
                    public void remove() {
                        if (last == null) throw new IllegalStateException();
                        session.removeAttribute(last);
                        last = null;
                    }
                };
            }

            @Override
            public int size() {
                int count = 0;
                Enumeration<String> names = session.getAttributeNames();
                while (names.hasMoreElements()) {
                    names.nextElement();
                    count++;
                }
                return count;
            }
        };
    }

    // Backing map entry that delegates to the HttpSession for get/set/remove
    private final class SessionEntry implements Entry<String, Object> {
        private final String key;

        SessionEntry(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return session.getAttribute(key);
        }

        @Override
        public Object setValue(Object value) {
            Object old = session.getAttribute(key);
            session.setAttribute(key, value);
            return old;
        }
    }

    @Override
    public int size() {
        int count = 0;
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            names.nextElement();
            count++;
        }
        return count;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return false;
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            if (names.nextElement().equals(key)) return true;
        }
        return false;
    }

    @Override
    public void clear() {
        // remove all attributes
        List<String> keys = new ArrayList<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            keys.add(names.nextElement());
        }
        for (String k : keys) session.removeAttribute(k);
    }
}
