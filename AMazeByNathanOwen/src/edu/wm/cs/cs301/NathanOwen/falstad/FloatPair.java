package edu.wm.cs.cs301.NathanOwen.falstad;

import java.io.Serializable;

/**
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 *
 */
public class FloatPair implements Serializable{
    public double p1, p2;
    
    /**
     * Constructor
     * @param pp1
     * @param pp2
     */
    FloatPair(double pp1, double pp2) {
	p1 = pp1;
	p2 = pp2;
    }
}
