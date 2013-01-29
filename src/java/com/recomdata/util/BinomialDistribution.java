/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/*
Copyright (C) 2001-2004  Kyle Siegrist, Dawn Duehring
Department of Mathematical Sciences
University of Alabama in Huntsville

This program is part of Virtual Laboratories in Probability and Statistics,
http://www.math.uah.edu/stat/.

This program is licensed under a Creative Commons License. Basically, you are free to copy,
distribute, and modify this program, and to make commercial use of the program.
However you must give proper attribution.
See http://creativecommons.org/licenses/by/2.0/ for more information.
 */
package com.recomdata.util;

import java.io.Serializable;

/**
 * This class models the binomial distribution with a specified number of trials and
 * probability of success. This distribution governs the number of success.
 * @author Kyle Siegrist
 * @author Dawn Duehring
 * @version August, 2003
 */
public class BinomialDistribution extends Distribution implements Serializable{

	private static final long serialVersionUID = 8499603665585417830L;
	//Variables
	private int trials;
	private double probability;

	/**
	 * This general constructor creates the binomial distribution with a specified
	 * number of trials and probability of success.
	 * @param n the number of trials
	 * @param p the probability of success
	 */
	public BinomialDistribution(int n, double p){
		setParameters(n, p);
	}

	/**
	 * This default constructor creates the binomial distribution with 10 trials
	 * and probability of success 0.5.
	 */
	public BinomialDistribution(){
		this(10, 0.5);
	}

	/**
	 * This method sets the number of trials and the probability of success, and
	 * computes the default domain.
	 */
	public void setParameters(int n, double p){
		//Correct invalid parameters
		if (n < 1) n = 1;
		if (p < 0) p = 0; else if (p > 1) p = 1;
		trials = n; probability = p;
		setDomain(0, trials, 1, DISCRETE);
	}

	/**
	 * This method sets the number of trails.
	 * @param n the number of trials
	 */
	public void setTrials(int n){
		setParameters(n, probability);
	}

	/**
	 * This method returns the number of trials.
	 * @return the number of trials
	 */
	public int getTrials(){
		return trials;
	}

	/**
	 * This method sets the probability of success.
	 * @param p the probability of success
	 */
	public void setProbability(double p){
		setParameters(trials, p);
	}

	/**
	 * This method returns the probability of success.
	 * @return the probability of success
	 */
	public double getProbability(){
		return probability;
	}

	/**
	 * This method computes the probability  density function in terms of the number
	 * of trials and the probability of success.
	 * @param x a number in the domain of the distribuiton
	 * @return the probability density at x
	 */
	public double getDensity(double x){
		int k = (int)Math.rint(x);
		if (k < 0 | k > trials) return 0;
		if (probability == 0){
			if (k == 0) return 1;
			else return 0;
		}
		else if (probability == 1){
			if (k == trials) return 1;
			else return 0;
		}
		else if (x<120) {       // Exact Calculation of Binomial Density
			return Functions.comb(trials, k) * Math.pow(probability, k) * Math.pow( 1 - probability, trials - k);
		}
		else {  //Normal Approximation to Binomial density
			if (x>=0 && x<=trials) {
				NormalDistribution ND = new NormalDistribution(getMean(), getSD());
				return ND.getDensity(x);
			}
			else return 0;
		}
	}

	/**
	 * This method returns the maximum value of the density function.
	 * @return the maximum value of the probability density function
	 */
	public double getMaxDensity(){
		double mode = Math.min(Math.floor((trials + 1) * probability), trials);
		return getDensity(mode);
	}

	/**
	 * This method computes the mean in terms of the number of trials and the probability
	 * of success.
	 * @return the mean
	 */
	public double getMean(){
		return trials * probability;
	}

	/**
	 * This method computes the variance in terms of the probability of success and
	 * the number of trials.
	 * @return the variance
	 */
	public double getVariance(){
		return trials * probability * (1 - probability);
	}

	/**
	 * This method computes the cumulative distribution function in terms of the beta
	 * cumulative distribution function.
	 * @param x a number in the domain of the distribution
	 * @return the cumulative probability at x
	 */
	public double getCDF(double x){
		if (x < 0) return 0;
		else if (x >= trials) return 1;
		else return 1 - Functions.betaCDF(probability, x + 1, trials - x);
	}

	/**
	 * This method simulates the a value from the distribution. This is done by
	 * simulating the Bernoulli trials and counting the number of successes.
	 * @return a simulated value from the distribution
	 */
	public double simulate(){
		int successes = 0;
		for (int i = 1; i <= trials; i++){
			if (Math.random() < probability) successes++;
		}
		return successes;
	}

	/**
	 * This method computes the probability generating function in terms of the
	 * number of trials and the probability of success.
	 * @param t a real number
	 * @return the probability generating function at t
	 */
	public double getPGF(double t){
		return Math.pow(1 - probability + probability * t, trials);
	}

	/**
	 * This method computes the moment generating function in terms of the
	 * probability generating function.
	 * @param t a real number
	 * @return the moment generating function at t
	 */
	public double getMGF(double t){
		return getPGF(Math.exp(t));
	}

	/**
	 * This method returns the factorial moment of a specified order.
	 * @param k the order
	 * @return the factorial moment of order k
	 */
	public double getFactorialMoment(int k){
		return Functions.perm(trials, k) * Math.pow(probability, k);
	}


	/**
	 * This method returns a string that gives the name of the distribution and the values of
	 * the parameters.
	 * @return a string giving the name of the distribution and the values of the parameters
	 */
	public String toString(){
		return "Binomial distribution [number of trials = " + trials
		+ ", probability of success = " + probability + "]";
	}

}

