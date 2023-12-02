package com.sgruendel.nextjs_dashboard.ui;

import lombok.Value;

@Value
public class BreadcrumbData {

    String label;

    String href;

    boolean isActive;
}
