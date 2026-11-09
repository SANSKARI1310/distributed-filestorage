package com.example.demo.fair;


public class TenantSchedulingState {
        
    private final int weight;
    private int deficit;

    public TenantSchedulingState(int weight , int deficit)
    {
        this.weight= weight;
        this.deficit=deficit;
    }

    public int getWeight()
    {
        return weight;
    }
    public int getDeficit()
    {
        return deficit;
    }
    public void addQuantum(int baseQuantum)
    {
        deficit = baseQuantum * weight;
    }
    public boolean canServe(int request)
    {
        return deficit>= request;
    }
    public void consume(int request)
    {
        deficit-=request;
    }
}
