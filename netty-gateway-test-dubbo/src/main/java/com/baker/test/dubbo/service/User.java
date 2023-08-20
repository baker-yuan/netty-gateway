package com.baker.test.dubbo.service;

import java.io.Serializable;

import lombok.Data;

@Data
public class User implements Serializable {
	private static final long serialVersionUID = -7651482540472411412L;
	private int id;
    private String name;
}
