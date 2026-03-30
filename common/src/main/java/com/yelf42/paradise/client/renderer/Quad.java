package com.yelf42.paradise.client.renderer;

public class Quad {
    public double x1, z1, x2, z2;
    public float u1, u2;

    public Quad(double x1, double z1, double x2, double z2, float u1, float u2) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.u1 = u1;
        this.u2 = u2;
    }
}
