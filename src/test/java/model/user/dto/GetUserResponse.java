package model.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private String createdAt;
    private String updatedAt;
    private List<GetUserAddressResponse> addresses;
}
