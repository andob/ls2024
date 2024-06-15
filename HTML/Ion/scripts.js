document.addEventListener('DOMContentLoaded', () => {
    const proveButton = document.getElementById('prove-button');
    const yoyoImage = document.getElementById('yoyo-image');

    proveButton.addEventListener('click', () => {
        yoyoImage.style.top = 'calc(100% - 50px)'; // Move yoyo to the bottom
        setTimeout(() => {
            yoyoImage.style.top = '0'; // Reset yoyo position
        }, 2000); // After 2 seconds
    });
});
