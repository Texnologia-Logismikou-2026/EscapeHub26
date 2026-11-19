package org.example.escapehubbackend.authService;

import org.example.escapehubbackend.dtoRegister.authResponse;
import org.example.escapehubbackend.dtoRegister.loginRequest;
import org.example.escapehubbackend.dtoRegister.registerRequest;
import org.example.escapehubbackend.model.User;
import org.example.escapehubbackend.repository.UserRepository;

public class authService {
    private final UserRepository userRepository;


    public authService(UserRepository userRepository) {                         //auth response on register
        this.userRepository = userRepository;
    }

    public authResponse register(registerRequest request){

        if (userRepository.existsByEmail(request.getEmail())){                  //fail logic
            throw new IllegalArgumentException("email is already registered");
        }

        User user=new User();                                       //success logic
        user.setUsername(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPass());
        return new authResponse("user registered","200");
    }
    public authResponse logIn(loginRequest loginRequest){                                                  //auth response on log in

        if (userRepository.existsByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPass())){
            return new authResponse("log in successfully","200");                           //success logic
        }else {
            throw new IllegalArgumentException("Email or password is invalid");                            //fail logic
        }

    }

}
