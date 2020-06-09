package com.azure.async99percentile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerModel {
	@JsonProperty(value = "id")
String id;
	@JsonProperty(value = "name")
String name;
	@JsonProperty(value = "lastName")
	     String lastName;
	@JsonProperty(value = "address")
	     String address;
}
