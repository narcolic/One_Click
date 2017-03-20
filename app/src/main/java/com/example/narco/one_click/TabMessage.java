package com.example.narco.one_click;


class TabMessage {
    public static String get(int menuItemId, boolean isReselection) {
        String message = "Content for ";

        switch (menuItemId) {
            case R.id.tab_suggestions:
                message += "Suggestions";
                break;
            case R.id.tab_map:
                message += "Map";
                break;
            case R.id.tab_makemyday:
                message += "Make My Day";
                break;
        }

        if (isReselection) {
            message += "";
        }

        return message;
    }
}