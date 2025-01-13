import {API_BASE_URL} from "./config.js";

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const phoneInput = document.getElementById('phone');
    const passwordInput = document.getElementById('password');

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault(); // 阻止表单的默认提交行为

        const phone = phoneInput.value.trim();
        const password = passwordInput.value.trim();


        const user = {
            password: password,
            phoneNumber: phone
        };

        // 发送AJAX请求到后台的login接口
        fetch(`${API_BASE_URL}/user/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(user)
        })
            .then(response => response.json())
            .then(data => {

                if (data.status == 200) {
                    // 登录成功，可以重定向到其他页面或显示成功消息
                    alert('登录成功！');
                    localStorage.setItem("user", JSON.stringify(data.result));
                    window.location.href = 'userInfo.html'; // 重定向到仪表盘或其他页面
                } else {
                    // 登录失败，显示错误消息
                    console.log('登录失败：', data);
                    alert('登录失败：' + data.message);
                }
            })
            .catch(error => {
                console.error('请求错误:', error);
                alert('请求错误，请稍后再试。');
            });
    });
});