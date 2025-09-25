import axios from 'axios'
import { useState } from 'react';
import '../login.css'

export function Login () {
    const [form, setForm] = useState({ username: "", password: "" });
    const [error, setError] = useState({username: "", password: ""});
    const [allow, setAllow] = useState("");

    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value})
    };

    const handleSubmit = async(e) => {
        e.preventDefault(); // prevent the page from reloading 

        if (!validateForm(form)) {  // check that the username and password are of correct format 
            console.log("Validation failed:", error);
            return;
        }

        try {
            const postResponse = await axios.post(`${backendUrl}/api/auth/login`, form);
            console.log("User successfully logs in.", response);

            // saving information on the webpage (?)
            localStorage.setItem('username', postResponse.data.username);
            localStorage.setItem('userId', postResponse.data.userId);
            localStorage.setItem('token', postResponse.data.token);

            // redirect user to homepage
            window.location.href = "/";

        } catch (postError) {
            if (postError.response?.status == "401") {
                console.log("Unable to authenticate user.");
                setAllow("Username or password is invalid.")

            }
        }
  };

    /*
     *  Method performs basic validation for username and password. 
    */
    function validateForm (form) {
        let tempErrors = ({username: "", password: ""});

        const usernameValidChars = /^[a-zA-Z0-9._]+$/;
        const passwordValidChars = /^[a-zA-Z0-9#$]+$/;

        if (form.username == "" || form.password == "") {
            console.log("User submitted an empty form.");
            tempErrors.username = "Username cannot be empty";
            tempErrors.password = "Password cannot be empty";  

        } else if (form.username.includes(" ")) {
            tempErrors.username = "Please enter a valid username.";            

        } else if (!usernameValidChars.test(form.username)) {
            tempErrors.username = "Please enter a valid username.";
 
        } else if (form.password.length < 8 || form.password.length > 20) {
            tempErrors.password = "Please enter a valid password";   

        } else if (!passwordValidChars.test(form.password)) {
            tempErrors.password = "Please enter a valid password";   
        }

        setError(tempErrors);

        return Object.values(tempErrors).every((msg) => (msg) == "");

    }

    return (
        <div className='login-page'>
            <h1>Welcome Back!</h1>
            <form className='login-form' onSubmit={handleSubmit}>

            <div className='login-section'>
            <h3>Username</h3>
            <input
                type='text'
                className='credentials-box'
                id='username'
                name='username'
                value={form.username}
                placeholder='Enter your username'
                onChange={handleChange}
            />

            {error.username && <p className='error-message-username'>{error.username}</p> }
            </div>

            <div className="login-section">
            <h3>Password</h3>
            <input
                type='password'
                className='credentials-box'
                id='passHash'
                name='password'
                value={form.password}
                placeholder='Enter your password'
                onChange={handleChange}
            />

            {error.password && <p className='error-message-password'>{error.password}</p> }
            </div>

            <button 
                type='submit'
                className='login-button'>
                Log In
            </button>

            </form>
        </div>
    )

}
