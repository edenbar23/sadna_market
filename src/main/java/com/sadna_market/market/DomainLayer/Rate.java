package com.sadna_market.market.DomainLayer;

/**
 * The Rate class represents the rating of a product.
 * It keeps track of the number of users who ranked the product
 * and the average rank value.
 */
public class Rate {
    private int numOfRanks;  // number of users ranked this product
    private double rateVal;  // the average rank

    /**
     * Default constructor that initializes the fields to 0.
     */
    public Rate() {
        this.numOfRanks = 0;
        this.rateVal = 0.0;
    }

    /**
     * Constructor with parameters.
     *
     * @param numOfRanks the initial number of ranks
     * @param rateVal the initial rate value (average)
     */
    public Rate(int numOfRanks, double rateVal) {
        this.numOfRanks = numOfRanks;
        this.rateVal = rateVal;
    }

    /**
     * Updates the rank by adding a new rating.
     * Increases the number of ranks by 1 and recalculates the average.
     *
     * @param newRank the new rank to add
     */
    public void addRank(double newRank) {
        // Calculate the total sum of all previous ranks
        double totalSum = rateVal * numOfRanks;

        // Increase the number of ranks
        numOfRanks++;

        // Add the new rank to the total sum
        totalSum += newRank;

        // Recalculate the average
        rateVal = totalSum / numOfRanks;
    }

    public void updateRank(double oldRank, double newRank) {
        // Calculate the total sum of all previous ranks
        double totalSum = rateVal * numOfRanks;

        // Update the total sum by removing the old rank and adding the new rank
        totalSum = totalSum - oldRank + newRank;

        // Recalculate the average
        rateVal = totalSum / numOfRanks;
    }

    /**
     * Gets the number of ranks.
     *
     * @return the number of ranks
     */
    public int getNumOfRanks() {
        return numOfRanks;
    }

    /**
     * Gets the average rate value.
     *
     * @return the average rate value
     */
    public double getRateVal() {
        return rateVal;
    }

}