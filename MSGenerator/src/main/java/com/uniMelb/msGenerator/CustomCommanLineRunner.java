package com.uniMelb.msGenerator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.uniMelb.msGenerator.components.AppComponent;

@Component
public class CustomCommanLineRunner implements CommandLineRunner{

    private final AppComponent appComponent;

    public CustomCommanLineRunner(AppComponent appComponent) {
        this.appComponent = appComponent;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Customer Runner Class Invoking....");
        appComponent.initiateIdentification();
    }
    
}
