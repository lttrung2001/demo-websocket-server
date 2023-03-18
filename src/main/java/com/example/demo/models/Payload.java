package com.example.demo.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class Payload implements Serializable {
    private String topic;
    private String action;
    private String data;
}
