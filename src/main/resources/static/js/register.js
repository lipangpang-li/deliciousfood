import {API_BASE_URL} from "./config.js";
document.addEventListener('DOMContentLoaded', function() {

    document.getElementById('registerForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 阻止表单默认提交行为

        // 获取表单数据
        const formData = new FormData(this);
        if (formData.get('username') === '' || formData.get('email') === '' || formData.get('password') === '' || formData.get('phone') === '') {
            alert("必填项不能为空");
            return;
        }

        let confirm = formData.get('confirm-password');

        if (confirm !== formData.get('password')) {
            alert("两次密码不一致");
            return;
        }

        const user = {
            username: formData.get('username'),
            email: formData.get('email'),
            password: formData.get('password'),
            phoneNumber: formData.get('phone')
        };

        // 发送数据到后台的 register 接口
        fetch(`${API_BASE_URL}/user/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer YOUR_JWT_TOKEN'
            },
            body: JSON.stringify(user)
        })
        .then(response => response.json())
        .then(data => {
            if(data.status == 999 ){
                alert( data.message );

            }else {

                console.log('Success:', data);
                alert('注册成功!');
                window.location.href = 'login.html';
            }

        })
        .catch((error) => {
            console.error('Error:', error);
            alert('注册失败，请重试。');
        });
    });
});
