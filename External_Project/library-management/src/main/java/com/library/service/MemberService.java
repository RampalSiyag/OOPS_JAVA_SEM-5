package com.library.service;

import com.library.exception.BusinessRuleException;
import com.library.model.Member;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository, LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> search(String query) {
        if (query == null || query.isBlank()) {
            return memberRepository.findAllByOrderByNameAsc();
        }
        String q = query.trim();
        return memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(q, q);
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return memberRepository.count();
    }

    public Member create(Member form) {
        form.setEmail(form.getEmail().trim());
        ensureEmailIsFree(form.getEmail(), null);
        Member member = new Member();
        copyFields(form, member);
        return memberRepository.save(member);
    }

    public Member update(Long id, Member form) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Member not found"));
        form.setEmail(form.getEmail().trim());
        ensureEmailIsFree(form.getEmail(), id);
        copyFields(form, member);
        return memberRepository.save(member);
    }

    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Member not found"));
        if (loanRepository.existsByMember(member)) {
            throw new BusinessRuleException(member.getName() + " has loan history and cannot be deleted");
        }
        memberRepository.delete(member);
    }

    private void ensureEmailIsFree(String email, Long currentId) {
        memberRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BusinessRuleException("A member with email " + email + " already exists");
            }
        });
    }

    private void copyFields(Member from, Member to) {
        to.setName(from.getName().trim());
        to.setEmail(from.getEmail());
        to.setPhone(from.getPhone() == null ? null : from.getPhone().trim());
    }
}
