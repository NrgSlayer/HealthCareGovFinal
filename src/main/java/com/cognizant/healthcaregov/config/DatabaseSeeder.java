package com.cognizant.healthcaregov.config;

import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public void run(String... args) throws Exception
    {
        if(userRepository.count()==0)
        {
            User user=new User();
            user.setEmail("shiva09@gmail.com");
            user.setPassword(passwordEncoder.encode("shiva098"));
            user.setStatus("APPROVED");
            user.setPhone("9999888877");
            user.setName("Shiva");
            user.setRole("DOCTOR");
            userRepository.save(user);

            user=new User();
            user.setEmail("akhil09@gmail.com");
            user.setPassword(passwordEncoder.encode("akhil098"));
            user.setStatus("APPROVED");
            user.setPhone("9999888866");
            user.setName("Akhil");
            user.setRole("DOCTOR");
            userRepository.save(user);

            User admin =new User();
            admin.setEmail("karthik09@gmail.com");
            admin.setPassword(passwordEncoder.encode("karthik098"));
            admin.setStatus("APPROVED");
            admin.setPhone("9999888877");
            admin.setName("Karthik");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            admin =new User();
            admin.setEmail("shashank09@gmail.com");
            admin.setPassword(passwordEncoder.encode("shashank098"));
            admin.setStatus("APPROVED");
            admin.setPhone("9999888866");
            admin.setName("Shashank");
            admin.setRole("ADMIN");
            userRepository.save(admin);


            User pmanager =new User();
            pmanager.setEmail("harshith09@gmail.com");
            pmanager.setPassword(passwordEncoder.encode("harshith098"));
            pmanager.setStatus("APPROVED");
            pmanager.setPhone("9999888877");
            pmanager.setName("Harshith");
            pmanager.setRole("PROGRAM_MANAGER");
            userRepository.save(pmanager);

            User pmanager2 =new User();
            pmanager2.setEmail("krish09@gmail.com");
            pmanager2.setPassword(passwordEncoder.encode("krish098"));
            pmanager2.setStatus("APPROVED");
            pmanager2.setPhone("9999888866");
            pmanager2.setName("Krish");
            pmanager2.setRole("PROGRAM_MANAGER");
            userRepository.save(pmanager2);

            User offcier =new User();
            offcier.setEmail("mahendhar09@gmail.com");
            offcier.setPassword(passwordEncoder.encode("mahendhar098"));
            offcier.setStatus("APPROVED");
            offcier.setPhone("9999888877");
            offcier.setName("Mahendhar");
            offcier.setRole("COMP_OFFICER");
            userRepository.save(offcier);

            User offcier2 =new User();
            offcier2.setEmail("priyank09@gmail.com");
            offcier2.setPassword(passwordEncoder.encode("priyank098"));
            offcier2.setStatus("APPROVED");
            offcier2.setPhone("9999888866");
            offcier2.setName("Priyank");
            offcier2.setRole("COMP_OFFICER");
            userRepository.save(offcier2);

            User auditor =new User();
            auditor.setEmail("vinay09@gmail.com");
            auditor.setPassword(passwordEncoder.encode("vinay098"));
            auditor.setStatus("APPROVED");
            auditor.setPhone("9999888877");
            auditor.setName("Vinay");
            auditor.setRole("AUDITOR");
            userRepository.save(auditor);

            User auditor2 =new User();
            auditor2.setEmail("ayush09@gmail.com");
            auditor2.setPassword(passwordEncoder.encode("ayush098"));
            auditor2.setStatus("APPROVED");
            auditor2.setPhone("9999888866");
            auditor2.setName("Ayush");
            auditor2.setRole("AUDITOR");
            userRepository.save(auditor2);
        }
    }
}
