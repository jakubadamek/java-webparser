package com.jakubadamek.robotemil.entities;

public class HotelPrice {
    private String hotel;
    private Integer price;
    private Short place;

    /**
     * @return the hotel
     */
    public String getHotel() {
	return hotel;
    }

    /**
     * @param hotel the hotel to set
     */
    public void setHotel(String hotel) {
	this.hotel = hotel;
    }

    /**
     * @return the price
     */
    public Integer getPrice() {
	return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(Integer price) {
	this.price = price;
    }

    /**
     * @return the place
     */
    public Short getPlace() {
	return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(Short place) {
	this.place = place;
    }
}
