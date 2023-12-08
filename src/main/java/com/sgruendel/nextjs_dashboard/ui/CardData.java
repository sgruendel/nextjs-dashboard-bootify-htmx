package com.sgruendel.nextjs_dashboard.ui;

import lombok.Value;

@Value
public class CardData<T> {

    String icon;

    String title;

    T value;
}