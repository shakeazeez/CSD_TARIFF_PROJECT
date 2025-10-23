package com.user.service;

import com.user.dto.MemberInfoDTO;
import com.user.user.MemberUser;
import com.user.user.MemberUserRepo;
import com.user.user.User;
import com.user.user.UserRepo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberUserServiceImpl implements MemberUserService {

    private final MemberUserRepo memberUserRepo;
    private final UserRepo userRepo;

    public MemberUserServiceImpl(MemberUserRepo memberUserRepo, UserRepo userRepo) {
        this.memberUserRepo = memberUserRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public List<Integer> addPinnedTariff(String username, Integer tariffId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user instanceof MemberUser memberUser) {
            if (memberUser.getPinnedTariffId().size() >= 3) {
                throw new IllegalStateException("Cannot pin more than 3 tariffs");
            }

            if (!memberUser.getPinnedTariffId().contains(tariffId)) {
                memberUser.getPinnedTariffId().add(tariffId);
                memberUserRepo.save(memberUser);
            }

            return memberUser.getPinnedTariffId();
        }

        throw new IllegalAccessError("This feature is not available for this user");
    }

    @Transactional
    public List<Integer> removePinnedTariff(String username, Integer tariffId) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user instanceof MemberUser memberUser) {
            if (memberUser.getPinnedTariffId().contains(tariffId)) {
                memberUser.getPinnedTariffId().remove(Integer.valueOf(tariffId));
                memberUserRepo.save(memberUser);
            }
    
            return memberUser.getPinnedTariffId();
        }
        
        throw new IllegalAccessError("This feature is not available for this user");
    }
    
    public MemberInfoDTO getPinnedTariff(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        if (user instanceof MemberUser memberUser) {
            return new MemberInfoDTO(memberUser.getPinnedTariffId());
        }
        
        throw new IllegalAccessError("Not a general user");
    }
}
