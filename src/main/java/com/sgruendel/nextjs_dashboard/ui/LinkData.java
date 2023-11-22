package com.sgruendel.nextjs_dashboard.ui;

import lombok.Value;

@Value
public class LinkData {

    String name;

    String href;

    String icon;

    boolean isCurrent;
}