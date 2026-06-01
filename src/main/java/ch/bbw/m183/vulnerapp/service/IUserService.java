package ch.bbw.m183.vulnerapp.service;

import ch.bbw.m183.vulnerapp.datamodel.UserEntity;

public interface IUserService {

	UserEntity whoami(String username, String password);
}

