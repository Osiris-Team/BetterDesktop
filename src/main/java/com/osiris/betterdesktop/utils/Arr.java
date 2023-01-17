package com.osiris.betterdesktop.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class Arr {

    public static <T> HashSet<T> hashSet(T... values) {
        HashSet<T> hashSet = new HashSet<>();
        if (values != null)
            for (T t : values) {
                hashSet.add(t);
            }
        return hashSet;
    }

    public static <T> void flip(List<T> list) {
        List<T> newList = new ArrayList<>(list.size());
        for (int i = list.size() - 1; i >= 0; i--) {
            newList.add(list.get(i));
        }
        list.clear();
        list.addAll(newList);
    }

    public static <T> void removeIf(List<T> list, Predicate<T> predicate) {
        List<T> removableObjects = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t))
                removableObjects.add(t);
        }
        list.removeAll(removableObjects);
    }
}
