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
 * Copyright (C) 2001-2004  Kyle Siegrist, Dawn Duehring
 * Department of Mathematical Sciences
 * University of Alabama in Huntsville
 *
 * This program is part of Virtual Laboratories in Probability and Statistics,
 * http://www.math.uah.edu/stat/.
 * 
 * This program is licensed under a Creative Commons License. Basically, you are free to copy,
 * distribute, and modify this program, and to make commercial use of the program.
 * However you must give proper attribution.
 * See http://creativecommons.org/licenses/by/2.0/ for more information.
 */
package com.recomdata.util;

import java.io.Serializable;

/**
 * This class encapsulates the normal distribution with specified parameters.
 * @author Kyle Siegrist
 * @author Dawn Duehring
 * @version August, 2003
 */
public class NormalDistribution extends Distribution implements Serializable {

	private static final long serialVersionUID = 5310804038054903917L;
	//Paramters
	public final static double SQRT2PI = Math.sqrt(2 * Math.PI);
	private double location, scale, c;

	/**
	 * This general constructor creates a new normal distribution with specified
	 * parameter values.
	 * @param m the location parameter
	 * @param s the scale parameter
	 */
	public NormalDistribution(double m, double s){
		setParameters(m, s);
	}

	/**
	 * This default constructor creates a new standard normal distribution (with
	 * location parameter 0 and scale parameter 1).
	 */
	public NormalDistribution(){
		this(0, 1);
	}

	/**
	 * This method sets the parameters and defines the default domain.
	 * @param m the location parameter
	 * @param s the scale parameter
	 */
	public void setParameters(double m, double s){
		double lower, upper, width;
		//Correct for invalid scale
		if (s < 0) s = 1;
		location = m; scale = s;
		c = SQRT2PI * scale;
		upper = location + 4 * scale;
		lower = location - 4 * scale;
		width = (upper - lower) / 100;
		setDomain(lower, upper, width, CONTINUOUS);
	}

	/**
        This method defines the probability density function.
	 * @param x a number in the domain of the distribution
	 * @return the probability density at x
	 */
	public double getDensity(double x){
		double z = (x - location) / scale;
		return Math.exp(- z * z / 2) / c;
	}

	/**
	 * This method returns the maximum value of the density function.
	 * @return the maximum value of the probability density function
	 */
	public double getMaxDensity(){
		return getDensity(location);
	}

	/**
	 * This method returns the median, which is the same as the location parameter.
	 * @return the median
	 */
	public double getMedian(){
		return location;
	}

	/**
	 * This method returns the mean, which is the same as the location parameter.
	 * @return the mean
	 */
	public double getMean(){
		return location;
	}

	/**
	 * This method returns the variance of the distribution.
	 * @return the variance
	 */
	public double getVariance(){
		return scale * scale;
	}

	/**
	 * This method computes the central moment of a specifed order.
	 * @param n the order
	 * @return the central moment of order n
	 */
	public double getCentralMoment(int n){
		if (n == 2 * (n / 2)) return Functions.factorial(n) * Math.pow(scale, n) / (Functions.factorial(n / 2) * Math.pow(2, n / 2));
		else return 0;
	}

	/**
	 * This method computes the moment of a specified order about a specified point.
	 * @param a the center
	 * @param n the order
	 * @return the moment of order n about a
	 */
	public double getMoment(double a, int n){
		double sum = 0;
		for (int k = 0; k <= n; k++) sum = sum + Functions.comb(n, k) * getCentralMoment(k) * Math.pow(location - a, n - k);
		return sum;
	}

	/**
	 * This method returns the moment generating function.
	 * @param t a real number
	 * @return the moment generating function at t
	 */
	public double getMGF(double t){
		return Math.exp(location * t + scale * scale * t * t / 2);
	}

	/**
	 * This method simulates a value from the distribution.
	 * @return a simulated value from the distribution
	 */
	public double simulate(){
		double r = Math.sqrt(-2 * Math.log(Math.random()));
		double theta = 2 * Math.PI * Math.random();
		return location + scale * r * Math.cos(theta);
	}

	/**
	 * This method returns the location parameter.
	 * @return the location parameter
	 */
	public double getLocation(){
		return location;
	}

	/**
	 * This method sets the location parameter.
	 * @param m the location parameter
	 */
	public void setLocation(double m){
		setParameters(m, scale);
	}

	/**
	 * This method gets the scale parameter.
	 * @return the scale parameter
	 */
	public double getScale(){
		return scale;
	}

	/**
	 * This method sets the scale parameter.
	 * @param s the scale parameter
	 */
	public void setScale(double s){
		setParameters(location, s);
	}

	/**
	 * This method computes the cumulative distribution function.
	 * @param x a number in the domain of the distribution
	 * @return the cumulative probability at x
	 */
	public double getCDF(double x){
		double z = (x - location) / scale;
		if (z >= 0) return 0.5 + 0.5 * Functions.gammaCDF(z * z / 2, 0.5);
		else return 0.5 - 0.5 * Functions.gammaCDF(z * z / 2, 0.5);
	}

	/**
	 * This method returns a string that gives the name of the distribution and the values of
	 * the parameters.
	 * @return a string giving the name of the distribution and the values of the parameters
	 */
	public String toString(){
		return "Normal distribution [location = " + location + ", scale = " + scale + "]";
	}
}
