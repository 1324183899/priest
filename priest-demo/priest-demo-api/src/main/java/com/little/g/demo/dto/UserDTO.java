package com.little.g.demo.dto;

import com.little.g.demo.api.UserService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class UserDTO implements Serializable {
    @NotNull
    private Integer id;
    @Size(max = 20,message = "myName{test}",groups = UserService.Add.class)
    private String myName;
    @NotNull(groups = UserService.Update.class)
    private Integer age;

    private String mobile;
    private Long createTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName == null ? null : myName.trim();
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }


}
