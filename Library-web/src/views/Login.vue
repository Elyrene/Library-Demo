<script setup lang="ts">

</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">登录</h2>
      
      <div class="login-form">
        <div class="form-group">
          <label for="username" class="form-label">用户名</label>
          <input
            type="text"
            id="username"
            class="form-input"
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password" class="form-label">密码</label>
          <input
            type="password"
            id="password"
            class="form-input"
            placeholder="请输入密码"
            required
          />
        </div>
        
        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox"  />
            <span>记住我</span>
          </label>
        </div>
        
        <button type="submit" class="login-button">
          登录
        </button>
        
      </div>
      
      <div class="login-footer">
        <p>还没有账号？<a href="#" class="register-link">立即注册</a></p>
      </div>
    </div>
  </div>
</template>

<style scoped>


.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-title {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-size: 1.5rem;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e1e5e9;
  border-radius: 5px;
  font-size: 1rem;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
}

.form-options {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input {
  margin-right: 0.5rem;
}

.login-button {
  background: #667eea;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  width: 100%;
  cursor: pointer;
  transition: background 0.3s;
  box-sizing: border-box;
}

.login-button:hover {
  background: #5a67d8;
}

.demo-button {
  background: #4caf50;
  color: white;
  padding: 0.75rem;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  width: 100%;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 0.5rem;
}

.demo-button:hover {
  background: #45a049;
}

.login-footer {
  text-align: center;
  margin-top: 1.5rem;
}

.register-link {
  color: #667eea;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
