import {API_BASE_URL} from "./config.js";

// search.js
document.addEventListener('DOMContentLoaded', function() {

    // 获取URL中的查询参数
    const urlParams = new URLSearchParams(window.location.search);
    const query = urlParams.get('query');


    if (query) {
        // 设置搜索框的值
        document.getElementById('search-box').value = query;

        // 发起请求以获取搜索结果
        fetch(`${API_BASE_URL}/deliciousFood/getBySomeThing?something=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(data => {
                const resultsContainer = document.getElementById('results-container');
                resultsContainer.innerHTML = ''; // 清空之前的搜索结果


                if (data.results && data.results.length > 0) {

                    data.results.forEach(result => {
                        const resultItem = document.createElement('div');
                        resultItem.className = 'result-item';
                        resultItem.innerHTML = `
                            <h3>${result.name}</h3>
                            <p>${result.introduce}</p>
                        `;
                        resultsContainer.appendChild(resultItem);
                    });
                } else {

                    resultsContainer.innerHTML = '<p>没有找到相关结果。</p>';
                }
            })
            .catch(error => {
                console.error('Error fetching search results:', error);
                const resultsContainer = document.getElementById('results-container');
                resultsContainer.innerHTML = '<p>无法获取搜索结果，请稍后再试。</p>';
            });
    } else {
        const resultsContainer = document.getElementById('results-container');
        resultsContainer.innerHTML = '<p>请输入搜索关键词。</p>';
    }

    // 处理搜索按钮点击事件
    const searchBox = document.getElementById('search-box');
    const searchBtn = document.getElementById('search-btn');

    searchBtn.addEventListener('click', function() {
        const query = searchBox.value.trim();
        if (query) {
            // 跳转到 search.html 并传递查询参数
            window.location.href = `search.html?query=${encodeURIComponent(query)}`;
        } else {
            alert('请输入搜索关键词');
        }
    });
});
