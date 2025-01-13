document.addEventListener('DOMContentLoaded', function() {
    const profileButton = document.getElementById('profileButton');
    const addRecipeButton = document.getElementById('addRecipeButton');
    const myRecommendationsButton = document.getElementById('myRecommendationsButton');
    const addIngredientsButton = document.getElementById('addIngredientsButton');
    const content = document.getElementById('content');

    profileButton.addEventListener('click', function() {
        content.innerHTML = `
                    <h2>个人信息</h2>
                    <p>这里是个人信息页面。</p>
                `;
    });

    addRecipeButton.addEventListener('click', function() {
        content.innerHTML = `
                    <h2>添加食谱</h2>
                    <p>这里是添加食谱页面。</p>
                `;
    });

    myRecommendationsButton.addEventListener('click', function() {
        content.innerHTML = `
                    <h2>我的推荐</h2>
                    <p>这里是我的推荐页面。</p>
                `;
    });

    addIngredientsButton.addEventListener('click', function() {
        content.innerHTML = `
                    <h2>添加食材</h2>
                    <p>这里是添加食材页面。</p>
                `;
    });
});