package com.ami.service;

import java.util.List;

import com.ami.dto.requests.AdminUpdateUserRequestDto;
import com.ami.dto.requests.CreateUserRequest;
import com.ami.dto.requests.UpdateProfileRequestDto;
import com.ami.dto.responses.AdminUpdateUserResponseDto;
import com.ami.dto.responses.CreateUserResponseDto;
import com.ami.dto.responses.CreationOptionsResponse;
import com.ami.dto.responses.MyInfoResponseDto;
import com.ami.dto.responses.PagedUserResponseDto;
import com.ami.dto.responses.UserDetailsResponseDto;
import com.ami.dto.responses.UserListResponseDto;
import com.ami.enums.RoleType;
import com.ami.enums.SourceType;

public interface UserService {

	CreateUserResponseDto createUser(CreateUserRequest request);

	CreationOptionsResponse getCreationOptions();

	PagedUserResponseDto getUsers(RoleType role, int page, int size);

	UserDetailsResponseDto getUserDetails(Long userId);

	MyInfoResponseDto getMyInfo();

	AdminUpdateUserResponseDto getUserForAdminUpdate(Long userId);

	UserDetailsResponseDto updateProfile(UpdateProfileRequestDto request);

	AdminUpdateUserResponseDto adminUpdateUser(Long userId, AdminUpdateUserRequestDto request);

	String softDeleteUser(Long userId);

	String hardDeleteUser(Long userId);

	PagedUserResponseDto searchUsers(String keyword, int page, int size);

	PagedUserResponseDto getUsersByStatus(Boolean active, int page, int size);

	List<UserListResponseDto> getEligibleAdminsBySource(SourceType sourceType, String search);

}