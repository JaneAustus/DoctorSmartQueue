document.addEventListener('DOMContentLoaded', function () {
    const registrationForm = document.getElementById('registrationForm');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    // Password Visibility Toggle
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);

            // Toggle eye icon
            const icon = this.querySelector('i');
            icon.classList.toggle('fa-eye');
            icon.classList.toggle('fa-eye-slash');
        });
    }

    // Form Validation and Submission
    if (registrationForm) {
        registrationForm.addEventListener('submit', function (event) {
            if (!registrationForm.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            } else {
                event.preventDefault();
                handleRegistration();
            }
            registrationForm.classList.add('was-validated');
        }, false);
    }

    function handleRegistration() {
        const formData = {
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            password: passwordInput.value
        };

        // Show a loading state on the button
        const submitBtn = registrationForm.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Creating Account...';

        console.log('Sending registration data:', formData);

        // Send registration data to the backend
        fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        })
            .then(async response => {
                const data = await response.text();
                if (response.ok) {
                    alert('Registration successful! Redirecting to login...');
                    window.location.href = 'index.html';
                } else {
                    throw new Error(data || 'Registration failed');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error: ' + error.message);

                // Re-enable button
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            });
    }
});
