import axios from 'axios'
import { useState } from 'react';
import { useState } from 'react';

function Login () {
    const [form, setForm] = useState({ username: "", password: "" });

    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value})
    };

    const handleSubmit = (e) => {
        e.preventDefault(); // prevent the page from reloading 
        const errorMessage = validateForm(form); // validate the inputs 
        if (errorMessage != null) {
            // certain display to notify users of problem
            // e.g. if password not long enough, display error message under password field
            return; // stop the submission
        }

        try {
            const response = axios.post(`${backendUrl}/api/auth/login`, form);
            console.log("User successfully logs in.", response);
        } catch (error) {
            console.log("Login Failed")
        }
  };

    /*
     *  Method performs basic validation for username and password. 
    */
    function validateForm (form) {

        // check that username does not contain spaces 
        if (form.username.includes(" ")) {
            return ("Username should not contain spaces.")
        } 

        // check that username only contains valid characters 
        const validChars = /^[a-zA-Z0-9._]+$/;
        if (!validChars.test(form.username)) {
            return ("Username contains invalid characters.")
        }

        // check that the password's length is valid
        if (form.password.length < 8 || form.password.length > 20) {
            return ("Password should be 8 - 20 characters long.")
        }    
        
        return null;

    }

}
