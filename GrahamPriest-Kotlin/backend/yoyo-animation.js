document.addEventListener('DOMContentLoaded', () => {
    const proveButton = document.getElementById('proveButton');
    const resultTextArea = document.getElementById('resultTextArea');
    const yoyo = document.getElementById('yoyo');
    const hand = document.getElementById('hand');

    proveButton.addEventListener('click', () => {
        // Move the yoyo and hand down
        yoyo.style.top = '400px';
        yoyo.style.transform = 'translateX(-50%) rotate(720deg)'; // Rotate 720 degrees
        hand.style.height = '200px'; // Contract the hand
        resultTextArea.style.display = 'none';

        setTimeout(() => {
            // Move the yoyo and hand back up
            yoyo.style.top = '100px';
            yoyo.style.transform = 'translateX(-50%) rotate(0deg)'; // Reset rotation
            hand.style.height = '100px'; // Reset the hand
            resultTextArea.style.display = 'block';
        }, 500); // After 2 seconds
    });
});
