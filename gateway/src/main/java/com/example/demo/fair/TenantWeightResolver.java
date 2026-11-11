package com.example.demo.fair;

import org.springframework.stereotype.Component;

@Component 
public class TenantWeightResolver {
    
    public int getWeight(String tenantId)
    {
        int ans=0;
        switch(tenantId)
        {
            case "TENANT-A":
                ans= 5;
            break;
            case "TENANT-B":
                ans= 3;
            break;
            case "TENANT-C":
                ans= 1;
            break;

            default:
                ans=1;
        }
        return ans;
    }
}
