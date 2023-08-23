document.getElementById("upload-file-button").addEventListener("click", function() {
    var uploadContent = document.getElementById("upload-content");
    var requestSignatureForm = document.getElementById("request-signature-form");
    if (uploadContent.style.display === 'none') {
        uploadContent.style.display = 'block';
        requestSignatureForm.style.display = 'none';
    } else {
        uploadContent.style.display = 'none';
    }
});

document.getElementById("request-signature-button").addEventListener("click", function() {
    var requestSignatureForm = document.getElementById("request-signature-form");
    var uploadContent = document.getElementById("upload-content");
    if (requestSignatureForm.style.display === 'none') {
        requestSignatureForm.style.display = 'block';
        uploadContent.style.display = 'none';
    } else {
        requestSignatureForm.style.display = 'none';
    }
});

var uploadArea = document.getElementById('upload-area');
var fileInput = document.getElementById('file-input');
var dragText = document.querySelector('.drag-text');

uploadArea.addEventListener('dragover', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
});

uploadArea.addEventListener('dragleave', function() {
    this.style.backgroundColor = 'transparent';
});

uploadArea.addEventListener('drop', function(e) {
    e.preventDefault();
    this.style.backgroundColor = 'transparent';
    fileInput.files = e.dataTransfer.files;
    updateFileNameDisplay();
});

document.querySelector('.browse-button').addEventListener('click', function() {
    fileInput.click();
});

fileInput.addEventListener('change', updateFileNameDisplay);

function updateFileNameDisplay() {
    var file = fileInput.files[0];
    if (file) {
        dragText.textContent = "Selected: " + file.name;
    } else {
        dragText.textContent = "Drag & Drop files here or";
    }
}

