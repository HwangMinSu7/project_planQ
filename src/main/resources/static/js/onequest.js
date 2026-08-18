document.addEventListener('DOMContentLoaded', function () {
  const button = document.querySelector('.nav-toggle');
  const menu = document.querySelector('#mainNav');

  if (!button || !menu) return;

  button.addEventListener('click', function () {
    const opened = menu.classList.toggle('open');
    button.setAttribute('aria-expanded', String(opened));
    button.textContent = opened ? '✕' : '☰';
  });
});
