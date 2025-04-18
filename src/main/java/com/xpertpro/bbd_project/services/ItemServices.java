package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemServices {
    @Autowired
    PackageRepository packageRepository;
}
