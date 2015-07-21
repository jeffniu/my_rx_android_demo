package com.ngy.myrxdemo.data;

import java.util.List;

/**
 * Created by niuguangyuan on 7/20/2015.
 */
public class Events {

    public static class RefreshUserListEvent {}

    public static class NewDisplayListEvent{
        public List<User> displayList;
        public NewDisplayListEvent(List<User> displayList) {
            this.displayList = displayList;
        }
    }

    public static class NewDataAddedEvent {
        public int size;
        public NewDataAddedEvent(int size) {
            this.size = size;
        }
    }

}
