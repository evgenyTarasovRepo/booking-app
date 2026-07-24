package com.booking.user.mapper;

import com.booking.user.dto.UserCreationDto;
import com.booking.user.dto.UserDto;
import com.booking.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userCreationDate", source = "createdAt")
    @Mapping(target = "isDeleted", source = "deleted")
    UserDto toUserDto(User user);

    List<UserDto> toUserDtoList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toUser(UserCreationDto userDto);
}
