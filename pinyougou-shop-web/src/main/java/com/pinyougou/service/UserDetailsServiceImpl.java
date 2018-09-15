package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

public class UserDetailsServiceImpl implements UserDetailsService  {
	private SellerService sellerService;
	
	public SellerService getSellerService() {
		return sellerService;
	}

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//构建角色列表
		List<GrantedAuthority> grantAuths=new ArrayList<GrantedAuthority>();
		grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		//查到商家对象
		TbSeller seller = sellerService.findOne(username);
		if(seller!=null) {
			//查到数据
			//商家状态为1审核通过才能登陆
			if(seller.getStatus().equals("1")) {
				return new User(username, seller.getPassword(),grantAuths);
			}else {
				return null;
			}
		}else {
			return null;
		}
	}

}
