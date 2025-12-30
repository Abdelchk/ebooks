package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface IUserController {
    ResponseEntity<?> createUser(@RequestBody UserController.CreateUserRequest request);
    ResponseEntity<String> updateUser(@RequestBody UserController.UpdateUserRequest request,
                                      @AuthenticationPrincipal UserDetails userDetails);
    ResponseEntity<String> checkSecurityQuestion(@RequestBody UserController.SecurityQuestionRequest request);
    ResponseEntity<String> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails);
    ResponseEntity<User> activateUser(@RequestParam String activationLink);
    ResponseEntity<String> changePassword(@RequestBody UserController.PasswordChangeRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails);
}
