const cartCount = document.getElementById('cart-count');
const addButtons = document.querySelectorAll('.add-to-cart');
const filterButtons = document.querySelectorAll('.filter-btn');
const productCards = document.querySelectorAll('.product-card');
const newsletterForm = document.querySelector('.newsletter');
const navToggle = document.querySelector('.nav-toggle');
const navLinks = document.querySelector('.nav-links');

let count = 0;

addButtons.forEach((button) => {
  button.addEventListener('click', () => {
    count += 1;
    cartCount.textContent = count;
    button.textContent = 'Added';
    button.disabled = true;
  });
});

filterButtons.forEach((button) => {
  button.addEventListener('click', () => {
    filterButtons.forEach((btn) => btn.classList.remove('active'));
    button.classList.add('active');

    const filter = button.dataset.filter;

    productCards.forEach((card) => {
      const matches = filter === 'all' || card.dataset.category === filter;
      card.style.display = matches ? 'block' : 'none';
    });
  });
});

newsletterForm.addEventListener('submit', (event) => {
  event.preventDefault();
  const input = newsletterForm.querySelector('input');
  if (input.value.trim()) {
    alert(`Thanks for subscribing, ${input.value}!`);
    input.value = '';
  }
});

navToggle.addEventListener('click', () => {
  navLinks.style.display = navLinks.style.display === 'flex' ? 'none' : 'flex';
  navLinks.style.flexDirection = 'column';
  navLinks.style.position = 'absolute';
  navLinks.style.top = '60px';
  navLinks.style.right = '1rem';
  navLinks.style.background = 'white';
  navLinks.style.padding = '1rem';
  navLinks.style.borderRadius = '0.8rem';
  navLinks.style.boxShadow = '0 12px 30px rgba(15, 23, 42, 0.08)';
});
