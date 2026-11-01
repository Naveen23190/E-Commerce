import React, { useEffect, useMemo, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ArrowRight, Check, Heart, LoaderCircle, LogOut, Menu, Minus, Plus, RefreshCw, Search, ShoppingBag, SlidersHorizontal, Trash2, X } from 'lucide-react';
import './style.css';
import './polish.css';

const API = import.meta.env.VITE_API_URL || '/api';
const emptyCart = { items: [], total: 0 };
const orderFlow = ['PLACED', 'CONFIRMED', 'PACKED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED'];
const readStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('store-user') || 'null');
  } catch {
    return null;
  }
};
const formatStatus = (status) => String(status || 'Not recorded').toLowerCase().replaceAll('_', ' ')
  .replace(/\b\w/g, character => character.toUpperCase());
const loadRazorpaySdk = () => new Promise((resolve, reject) => {
  if (window.Razorpay) {
    resolve();
    return;
  }
  const script = document.createElement('script');
  script.src = 'https://checkout.razorpay.com/v1/checkout.js';
  script.async = true;
  script.onload = () => resolve();
  script.onerror = () => reject(new Error('Could not load Razorpay test checkout. Check your internet connection.'));
  document.head.appendChild(script);
});
const orderTracking = (order) => {
  if (Array.isArray(order.tracking)) return order.tracking;
  const currentIndex = orderFlow.indexOf(order.status);
  const steps = orderFlow.map((status, index) => ({
    status,
    complete: currentIndex >= 0 && index <= currentIndex
  }));
  if (order.status === 'CANCELLED') steps.push({ status: 'CANCELLED', complete: true });
  return steps;
};
const formatPrice = (amount) => new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 2
}).format(Number(amount));
const fallbackImage = 'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1200&q=85';
const productImages = {
  'Everyday Ceramic Mug': 'https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?auto=format&fit=crop&w=900&q=85',
  'Canvas Weekender': 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=85',
  'Desk Lamp': 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=85',
  'Botanical Notebook': 'https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=900&q=85',
  'Linen Throw': 'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=900&q=85'
};
const productImage = (product) => {
  const imageUrl = product.imageUrl || '';
  if (imageUrl && !imageUrl.endsWith('/product-placeholder.svg')) return imageUrl;
  return productImages[product.name] || fallbackImage;
};
const handleImageError = (event) => {
  const image = event.currentTarget;
  if (image.dataset.fallbackApplied) {
    image.style.visibility = 'hidden';
    return;
  }
  image.dataset.fallbackApplied = 'true';
  image.src = fallbackImage;
};
const PaginationControls = ({ pageInfo, onPageChange, label }) => {
  if (!pageInfo || pageInfo.totalPages <= 1) return null;
  return <nav className="pagination-controls" aria-label={`${label} pagination`}>
    <button className="text-button" disabled={pageInfo.first} onClick={() => onPageChange(pageInfo.page - 1)}>Previous</button>
    <span>Page {pageInfo.page + 1} of {pageInfo.totalPages}</span>
    <button className="text-button" disabled={pageInfo.last} onClick={() => onPageChange(pageInfo.page + 1)}>Next</button>
  </nav>;
};

function App() {
  const [token, setToken] = useState(localStorage.getItem('store-token') || '');
  const [user, setUser] = useState(readStoredUser);
  const [products, setProducts] = useState([]);
  const [productPageInfo, setProductPageInfo] = useState(null);
  const [productPage, setProductPage] = useState(0);
  const [categories, setCategories] = useState(['All']);
  const [cart, setCart] = useState(emptyCart);
  const [orders, setOrders] = useState([]);
  const [ordersState, setOrdersState] = useState('idle');
  const [ordersPage, setOrdersPage] = useState(0);
  const [ordersPageInfo, setOrdersPageInfo] = useState(null);
  const [confirmedOrder, setConfirmedOrder] = useState(null);
  const [adminOrders, setAdminOrders] = useState([]);
  const [adminOrdersPage, setAdminOrdersPage] = useState(0);
  const [adminOrdersPageInfo, setAdminOrdersPageInfo] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [addressesState, setAddressesState] = useState('idle');
  const [selectedAddressId, setSelectedAddressId] = useState('');
  const [saveAddress, setSaveAddress] = useState(false);
  const [lowStockProducts, setLowStockProducts] = useState([]);
  const [inventoryProducts, setInventoryProducts] = useState([]);
  const [inventoryMovements, setInventoryMovements] = useState([]);
  const [inventoryProductId, setInventoryProductId] = useState('');
  const [inventoryAdjustment, setInventoryAdjustment] = useState({ quantityChange: '', reason: '' });
  const [activeProduct, setActiveProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });
  const [favorites, setFavorites] = useState(() => {
    try {
      const saved = JSON.parse(localStorage.getItem('store-favorites') || '[]');
      return Array.isArray(saved) ? saved : [];
    } catch {
      return [];
    }
  });
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('All');
  const [sortOrder, setSortOrder] = useState('featured');
  const [showFavorites, setShowFavorites] = useState(false);
  const [priceRange, setPriceRange] = useState({ minimum: '', maximum: '' });
  const [minimumRating, setMinimumRating] = useState('0');
  const [inStockOnly, setInStockOnly] = useState(false);
  const [view, setView] = useState('shop');
  const [drawer, setDrawer] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '' });
  const [address, setAddress] = useState('');
  const [pinCode, setPinCode] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [productForm, setProductForm] = useState(null);
  const orderRequestId = useRef(0);
  const adminOrderRequestId = useRef(0);
  const productRequestId = useRef(0);
  const toastTimer = useRef(null);

  const request = async (path, options = {}) => {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers }
    });
    if (response.status === 204) return null;
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      if (response.status === 401) throw new Error('Your session has expired. Sign out and sign in again.');
      if (response.status === 403) throw new Error('Your account does not have permission to perform this action. Sign in with an account that has the required role.');
      throw new Error(body.error || `Request failed (${response.status}). Please try again.`);
    }
    return body;
  };
  const loadProducts = async (page = productPage, size = view === 'manage' ? 20 : showFavorites ? 100 : 12) => {
    const requestId = ++productRequestId.current;
    try {
      const params = new URLSearchParams();
      if (query.trim()) params.set('query', query.trim());
      if (category !== 'All') params.set('category', category);
      if (priceRange.minimum !== '') params.set('minimumPrice', priceRange.minimum);
      if (priceRange.maximum !== '') params.set('maximumPrice', priceRange.maximum);
      params.set('minimumRating', minimumRating);
      if (inStockOnly) params.set('inStockOnly', 'true');
      params.set('page', String(page));
      params.set('size', String(size));
      params.set('sort', sortOrder.startsWith('price-') ? 'price' : 'name');
      params.set('direction', sortOrder === 'price-high' ? 'desc' : 'asc');
      const result = await request(`/products?${params}`);
      if (requestId === productRequestId.current) {
        setProducts(result.content);
        setProductPageInfo(result);
      }
    } catch (error) {
      if (requestId === productRequestId.current) flash(error.message);
    }
  };
  const loadCategories = async () => {
    try { setCategories(['All', ...await request('/products/categories')]); }
    catch (error) { flash(error.message); }
  };
  const loadCart = async () => { if (token) try { setCart(await request('/cart')); } catch (e) { flash(e.message); } else setCart(emptyCart); };
  const loadAddresses = async () => {
    setAddressesState('loading');
    try {
      const result = await request('/addresses');
      setAddresses(result);
      setAddressesState('loaded');
      const defaultAddress = result.find(address => address.defaultAddress);
      if (defaultAddress && !selectedAddressId) selectAddress(defaultAddress);
    } catch (e) {
      setAddressesState('error');
      flash(e.message);
    }
  };
  const selectAddress = (savedAddress) => {
    setSelectedAddressId(savedAddress ? String(savedAddress.id) : '');
    setAddress(savedAddress?.address || '');
    setPinCode(savedAddress?.pinCode || '');
  };
  const loadOrders = async (page = ordersPage) => {
    const requestId = ++orderRequestId.current;
    setOrdersState('loading');
    try {
      const result = await request(`/orders?page=${page}&size=5`);
      if (requestId !== orderRequestId.current) return;
      setOrders(result.content);
      setOrdersPageInfo(result);
      setOrdersState('loaded');
    } catch (e) {
      if (requestId !== orderRequestId.current) return;
      setOrdersState('error');
      flash(e.message);
    }
  };
  const loadAdminOrders = async (page = adminOrdersPage) => {
    const requestId = ++adminOrderRequestId.current;
    try {
      const result = await request(`/orders/admin?page=${page}&size=5`);
      if (requestId !== adminOrderRequestId.current) return;
      setAdminOrders(result.content);
      setAdminOrdersPageInfo(result);
    } catch (e) { if (requestId === adminOrderRequestId.current) flash(e.message); }
  };
  const loadInventory = async () => {
    try {
      const inventoryPage = await request('/products?size=100');
      setInventoryProducts(inventoryPage.content);
      setLowStockProducts(inventoryPage.content.filter(product => product.stock <= 5));
      setInventoryMovements(await request('/inventory/movements'));
    } catch (e) { flash(e.message); }
  };
  const flash = (message) => {
    if (toastTimer.current) window.clearTimeout(toastTimer.current);
    setNotice(message);
    toastTimer.current = window.setTimeout(() => setNotice(''), 3200);
  };
  useEffect(() => { loadCategories(); }, []);
  useEffect(() => { loadProducts(); }, [query, category, sortOrder, productPage, showFavorites, priceRange, minimumRating, inStockOnly, view]);
  useEffect(() => { loadCart(); }, [token]);
  useEffect(() => { if (view === 'orders' && token) loadOrders(ordersPage); }, [view, token, ordersPage]);
  useEffect(() => {
    if (!token) {
      orderRequestId.current++;
      setOrders([]);
      setOrdersState('idle');
    }
  }, [token]);
  useEffect(() => { if (view === 'manage' && token && user?.role === 'ADMIN') loadAdminOrders(adminOrdersPage); }, [view, token, user, adminOrdersPage]);
  useEffect(() => { if (view === 'manage' && token && user?.role === 'ADMIN') loadInventory(); }, [view, token, user]);
  useEffect(() => { if (drawer === 'cart' && token) loadAddresses(); }, [drawer, token]);
  useEffect(() => {
    if (!activeProduct) return;
    let isCurrentProduct = true;
    request(`/products/${activeProduct.id}/reviews`)
      .then(result => { if (isCurrentProduct) setReviews(result); })
      .catch(e => { if (isCurrentProduct) flash(e.message); });
    return () => { isCurrentProduct = false; };
  }, [activeProduct, token]);
  useEffect(() => () => {
    if (toastTimer.current) window.clearTimeout(toastTimer.current);
  }, []);
  useEffect(() => { localStorage.setItem('store-favorites', JSON.stringify(favorites)); }, [favorites]);

  const visibleProducts = useMemo(() => {
    return showFavorites ? products.filter(product => favorites.includes(product.id)) : products;
  }, [products, favorites, showFavorites]);
  const count = cart.items.reduce((sum, item) => sum + item.quantity, 0);
  const toggleFavorite = (product) => {
    setFavorites(current => current.includes(product.id)
      ? current.filter(id => id !== product.id)
      : [...current, product.id]);
  };
  const saveSession = (result) => {
    localStorage.setItem('store-token', result.token);
    const profile = { name: result.name, email: result.email, role: result.role };
    localStorage.setItem('store-user', JSON.stringify(profile)); setToken(result.token); setUser(profile); setDrawer('');
    flash(`Welcome, ${result.name.split(' ')[0]}.`);
  };
  const submitAuth = async (event) => {
    event.preventDefault(); setBusy(true);
    try { saveSession(await request(`/auth/${authMode}`, { method: 'POST', body: JSON.stringify(authForm) })); }
    catch (e) { flash(e.message); } finally { setBusy(false); }
  };
  const signOut = () => {
    localStorage.removeItem('store-token'); localStorage.removeItem('store-user');
    orderRequestId.current++;
    adminOrderRequestId.current++;
    setToken(''); setUser(null); setCart(emptyCart); setProductPage(0); setView('shop'); setDrawer(''); flash('You are signed out.');
    setAddresses([]); setAddressesState('idle'); setSelectedAddressId(''); setAddress(''); setPinCode(''); setSaveAddress(false);
    setOrdersPage(0); setAdminOrdersPage(0);
  };
  const browseCategory = (nextCategory) => {
    setProductPage(0);
    setCategory(nextCategory);
    setView('shop');
    setDrawer('');
    window.setTimeout(() => document.querySelector('#collection')?.scrollIntoView({ behavior: 'smooth' }), 0);
  };
  const addToCart = async (product) => {
    if (!token) { setDrawer('auth'); setAuthMode('login'); flash('Sign in to add items to your bag.'); return; }
    try { setCart(await request(`/cart/items/${product.id}`, { method: 'POST', body: JSON.stringify({ quantity: 1 }) })); flash(`${product.name} added to your bag.`); }
    catch (e) { flash(e.message); }
  };
  const changeQuantity = async (item, quantity) => {
    try {
      if (quantity < 1) setCart(await request(`/cart/items/${item.productId}`, { method: 'DELETE' }));
      else setCart(await request(`/cart/items/${item.productId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }));
    } catch (e) { flash(e.message); }
  };
  const deleteAddress = async (savedAddress) => {
    try {
      await request(`/addresses/${savedAddress.id}`, { method: 'DELETE' });
      const remaining = addresses.filter(item => item.id !== savedAddress.id);
      setAddresses(remaining);
      if (String(savedAddress.id) === selectedAddressId) selectAddress(null);
      flash('Saved address removed.');
    } catch (e) { flash(e.message); }
  };
  const makeDefaultAddress = async (savedAddress) => {
    try {
      const updated = await request(`/addresses/${savedAddress.id}/default`, { method: 'PUT' });
      setAddresses(current => current.map(item => ({ ...item, defaultAddress: item.id === updated.id })));
      flash('Default delivery address updated.');
    } catch (e) { flash(e.message); }
  };
  const openProduct = (product) => {
    setActiveProduct(product);
    setReviewForm({ rating: 5, comment: '' });
    setReviews([]);
  };
  const submitReview = async (event) => {
    event.preventDefault();
    if (!activeProduct) return;
    setBusy(true);
    try {
      const review = await request(`/products/${activeProduct.id}/reviews`, {
        method: 'POST',
        body: JSON.stringify({ rating: Number(reviewForm.rating), comment: reviewForm.comment })
      });
      setReviews(current => [review, ...current]);
      setReviewForm({ rating: 5, comment: '' });
      flash('Thank you for sharing your experience.');
    } catch (e) { flash(e.message); } finally { setBusy(false); }
  };
  const showOrderConfirmation = (createdOrder) => {
    orderRequestId.current++;
    setOrders(current => [createdOrder, ...current.filter(order => order.id !== createdOrder.id)]);
    setOrdersState('loaded');
    setOrdersPage(0);
    setConfirmedOrder(createdOrder);
    setCart(emptyCart);
    setAddress('');
    setPinCode('');
    setSelectedAddressId('');
    setSaveAddress(false);
    setDrawer('');
    setView('confirmation');
  };
  const saveCurrentAddress = async () => {
    if (!saveAddress || addresses.some(saved => saved.address === address.trim() && saved.pinCode === pinCode)) return;
    const savedAddress = await request('/addresses', {
      method: 'POST',
      body: JSON.stringify({ label: 'Delivery address', address: address.trim(), pinCode, defaultAddress: addresses.length === 0 })
    });
    setAddresses(current => [savedAddress, ...current]);
  };
  const checkout = async (event) => {
    event.preventDefault();
    setBusy(true);
    try {
      await saveCurrentAddress();
      if (paymentMethod !== 'COD') {
        await loadRazorpaySdk();
        const checkoutInfo = await request('/orders/razorpay/order', {
          method: 'POST',
          body: JSON.stringify({ shippingAddress: address, pinCode })
        });
        let paymentVerificationStarted = false;
        let paymentFailed = false;
        const gateway = new window.Razorpay({
          key: checkoutInfo.keyId,
          order_id: checkoutInfo.gatewayOrderId,
          amount: checkoutInfo.amount,
          currency: checkoutInfo.currency,
          name: checkoutInfo.merchantName,
          description: checkoutInfo.description,
          prefill: { email: checkoutInfo.prefillEmail },
          method: paymentMethod === 'UPI_TEST' ? { upi: true, card: false } : { upi: false, card: true },
          retry: { enabled: true, max_count: 3 },
          theme: { color: '#e1b966' },
          modal: {
            ondismiss: () => {
              setBusy(false);
              if (!paymentVerificationStarted && !paymentFailed)
                flash('Checkout closed. Your cart is unchanged; you can try again whenever you’re ready.');
            }
          },
          handler: async response => {
            paymentVerificationStarted = true;
            setBusy(true);
            try {
              const createdOrder = await request('/orders/razorpay/verify', {
                method: 'POST',
                body: JSON.stringify({
                  attemptId: checkoutInfo.attemptId,
                  razorpayOrderId: response.razorpay_order_id,
                  razorpayPaymentId: response.razorpay_payment_id,
                  razorpaySignature: response.razorpay_signature
                })
              });
              showOrderConfirmation(createdOrder);
              flash('Razorpay test payment successful. Your order is confirmed.');
            } catch (e) { flash(e.message); } finally { setBusy(false); }
          }
        });
        gateway.on('payment.failed', response => {
          paymentFailed = true;
          setBusy(false);
          flash(response.error?.description || 'The test payment did not go through. Your cart is unchanged; you can retry checkout.');
        });
        gateway.open();
        return;
      }
      const createdOrder = await request('/orders', {
        method: 'POST',
        body: JSON.stringify({ shippingAddress: address, pinCode, paymentMethod: 'COD' })
      });
      showOrderConfirmation(createdOrder);
      flash('Your order is placed. Pay on delivery.');
    } catch (e) { flash(e.message); await loadCart(); } finally { setBusy(false); }
  };
  const adjustInventory = async (event) => {
    event.preventDefault();
    setBusy(true);
    try {
      await request(`/products/${inventoryProductId}/inventory/adjust`, {
        method: 'POST',
        body: JSON.stringify({ quantityChange: Number(inventoryAdjustment.quantityChange), reason: inventoryAdjustment.reason })
      });
      setInventoryAdjustment({ quantityChange: '', reason: '' });
      await Promise.all([loadInventory(), loadProducts()]);
      flash('Inventory updated and recorded.');
    } catch (e) { flash(e.message); } finally { setBusy(false); }
  };
  const updateOrderStatus = async (order, status) => {
    try {
      const updated = await request(`/orders/${order.id}/status`, { method: 'PUT', body: JSON.stringify({ status }) });
      setAdminOrders(current => current.map(item => item.id === updated.id ? updated : item));
      setOrders(current => current.map(item => item.id === updated.id ? updated : item));
      flash(`Order ${String(order.id).padStart(5, '0')} updated.`);
    } catch (e) { flash(e.message); }
  };
  const persistProduct = async (event) => {
    event.preventDefault(); setBusy(true);
    const editing = Boolean(productForm.id);
    try {
      await request(`/products${editing ? `/${productForm.id}` : ''}`, {
        method: editing ? 'PUT' : 'POST', body: JSON.stringify({ ...productForm, price: Number(productForm.price), stock: Number(productForm.stock) })
      });
      setProductForm(null); setProductPage(0);
      await Promise.all([loadProducts(0, 20), loadCategories(), loadInventory()]);
      flash(editing ? 'Product updated.' : 'Product added.');
    } catch (e) { flash(e.message); } finally { setBusy(false); }
  };
  const removeProduct = async (product) => {
    if (!window.confirm(`Remove ${product.name}?`)) return;
    try {
      await request(`/products/${product.id}`, { method: 'DELETE' });
      const nextPage = products.length === 1 && productPage > 0 ? productPage - 1 : productPage;
      setProductPage(nextPage);
      await Promise.all([loadProducts(nextPage, 20), loadCategories(), loadInventory()]);
      flash('Product removed.');
    }
    catch (e) { flash(e.message); }
  };
  return <div className="app-shell">
    <div className="announcement"><span className="announcement-namaste">नमस्ते</span> Thoughtfully chosen for India <span>·</span> Good things for everyday living</div>
    <header className="header">
      <button className="mobile-menu icon-button" onClick={() => setDrawer(drawer ? '' : 'nav')} aria-label="Open menu"><Menu /></button>
      <a className="brand" href="#" onClick={e => { e.preventDefault(); setProductPage(0); setView('shop'); }}>E-<span>Cart</span><i>.</i></a>
      <nav className={`main-nav ${drawer === 'nav' ? 'nav-open' : ''}`}>
        <button className={view === 'shop' && category === 'All' ? 'active' : ''} onClick={() => browseCategory('All')}>Shop all</button>
        <button className={view === 'shop' && category === 'Home' ? 'active' : ''} onClick={() => browseCategory('Home')}>Home & living</button>
        <button className={view === 'shop' && category === 'Accessories' ? 'active' : ''} onClick={() => browseCategory('Accessories')}>Everyday carry</button>
        {user && <button className={view === 'orders' ? 'active' : ''} onClick={() => { setView('orders'); setDrawer(''); }}>My orders</button>}
        {user?.role === 'ADMIN' && <button className={view === 'manage' ? 'active' : ''} onClick={() => { setProductPage(0); setQuery(''); setCategory('All'); setSortOrder('featured'); setShowFavorites(false); setPriceRange({ minimum: '', maximum: '' }); setMinimumRating('0'); setInStockOnly(false); setView('manage'); setDrawer(''); }}>Manage shop</button>}
      </nav>
      <div className="header-actions">
        <button className="icon-button" aria-label="Search" onClick={() => document.querySelector('#search')?.focus()}><Search /></button>
        {user ? <button className="user-chip" onClick={signOut} title="Sign out"><span className="avatar">{user.name?.[0]}</span><span className="user-name">{user.name.split(' ')[0]}</span><LogOut size={15} /></button> :
          <button className="account-link" onClick={() => { setAuthMode('login'); setDrawer('auth'); }}>Sign in</button>}
        <button className="bag-button" onClick={() => setDrawer('cart')} aria-label={`Shopping bag, ${count} items`}><ShoppingBag size={19} /><span>Bag</span><b>{count}</b></button>
      </div>
    </header>
    {view === 'shop' && <>
      <section className="hero">
        <div className="hero-copy"><p className="eyebrow">MADE FOR THE EVERYDAY · भारत में चुना गया</p><h1>Good things.<br /><em>Made for living.</em></h1><p className="hero-hindi" lang="hi">हर दिन के लिए, आपके घर के लिए</p><p className="hero-sub">A considered collection for Indian homes, daily rituals, and the little moments that make a place yours.</p><button className="dark-button" onClick={() => document.querySelector('#collection')?.scrollIntoView({ behavior: 'smooth' })}>Explore the collection <ArrowRight size={16} /></button></div>
        <div className="hero-art"><div className="hero-circle"></div><img src={productImages['Linen Throw']} onError={handleImageError} alt="A warmly styled living room from our collection" /><span className="art-note">THE EVERYDAY<br />EDIT — Nº 01</span></div>
        <div className="hero-index">01 <span>/</span> 04</div>
      </section>
      <section className="value-strip"><div><span>01</span><strong>For chai & conversations</strong><p>Everyday pieces for shared moments.</p></div><div><span>02</span><strong>Thoughtfully chosen</strong><p>Considered details for Indian homes.</p></div><div><span>03</span><strong>Made for daily living</strong><p>Useful things, without the fuss.</p></div></section>
      <main id="collection" className="collection">
        <div className="section-heading"><div><p className="eyebrow">THE COLLECTION</p><h2>Find your <em>everyday.</em></h2></div><div className="result-note">{visibleProducts.length} shown · {productPageInfo?.totalElements || 0} total</div></div>
        <div className="catalog-tools"><div className="filters">{categories.map(item => <button key={item} onClick={() => { setProductPage(0); setCategory(item); setShowFavorites(false); }} className={!showFavorites && category === item ? 'selected' : ''}>{item}</button>)}</div><div className="catalog-actions"><label className="search-box"><Search size={16} /><input id="search" value={query} onChange={e => { setProductPage(0); setQuery(e.target.value); }} placeholder="Search the collection" /></label><label className="sort-control"><SlidersHorizontal size={15} /><select aria-label="Sort products" value={sortOrder} onChange={e => { setProductPage(0); setSortOrder(e.target.value); }}><option value="featured">Name: A to Z</option><option value="price-low">Price: low to high</option><option value="price-high">Price: high to low</option></select></label><button className={`favorites-filter ${showFavorites ? 'selected' : ''}`} onClick={() => { setProductPage(0); setShowFavorites(value => !value); }} aria-pressed={showFavorites}><Heart size={15} fill={showFavorites ? 'currentColor' : 'none'} /> Saved <span>{favorites.length}</span></button></div></div>
        <div className="catalog-refinements">
          <label>Min price <input type="number" min="0" inputMode="decimal" value={priceRange.minimum} onChange={e => { setProductPage(0); setPriceRange(current => ({ ...current, minimum: e.target.value })); }} placeholder="₹ Min" /></label>
          <label>Max price <input type="number" min="0" inputMode="decimal" value={priceRange.maximum} onChange={e => { setProductPage(0); setPriceRange(current => ({ ...current, maximum: e.target.value })); }} placeholder="₹ Max" /></label>
          <label>Rating <select value={minimumRating} onChange={e => { setProductPage(0); setMinimumRating(e.target.value); }}><option value="0">Any rating</option><option value="4">4 stars & up</option><option value="3">3 stars & up</option></select></label>
          <label className="stock-filter"><input type="checkbox" checked={inStockOnly} onChange={e => { setProductPage(0); setInStockOnly(e.target.checked); }} /> In stock only</label>
          {(priceRange.minimum || priceRange.maximum || minimumRating !== '0' || inStockOnly) && <button className="clear-filters" onClick={() => { setProductPage(0); setPriceRange({ minimum: '', maximum: '' }); setMinimumRating('0'); setInStockOnly(false); }}>Clear filters</button>}
        </div>
        <div className="product-grid">{visibleProducts.map((product, index) => <article className="product-card" key={product.id}>
          <div className="product-image"><img src={productImage(product)} onError={handleImageError} alt={product.name} /><span className="product-number">0{index + 1}</span><button className={`favorite-button ${favorites.includes(product.id) ? 'is-favorite' : ''}`} onClick={() => toggleFavorite(product)} aria-label={`${favorites.includes(product.id) ? 'Remove' : 'Save'} ${product.name}`} aria-pressed={favorites.includes(product.id)}><Heart size={17} fill={favorites.includes(product.id) ? 'currentColor' : 'none'} /></button><button className="quick-add" disabled={product.stock === 0} onClick={() => addToCart(product)} aria-label={`Add ${product.name} to bag`}><Plus size={20} /></button>{product.stock === 0 && <span className="sold-out">SOLD OUT</span>}</div>
          <div className="product-meta"><div><p className="product-category">{product.category}</p><h3>{product.name}</h3></div><span className="price">{formatPrice(product.price)}</span></div>
          <p className="product-description">{product.description}</p>
          <button className="product-rating" onClick={() => openProduct(product)} aria-label={`${product.reviewCount || 0} customer reviews`}><span>★</span> {Number(product.averageRating || 0) > 0 ? Number(product.averageRating).toFixed(1) : 'New'} <small>({product.reviewCount || 0})</small></button>
          <button className="details-link" onClick={() => openProduct(product)}>View details & reviews <ArrowRight size={14} /></button>
          {product.stock > 0 && product.stock <= 5 && <p className="stock-note">Only {product.stock} left</p>}
        </article>)}</div>
        <PaginationControls pageInfo={productPageInfo} onPageChange={setProductPage} label="Products" />
        {!visibleProducts.length && <div className="empty-state"><span>◌</span><h3>{showFavorites ? 'No saved pieces yet.' : 'Loading API....'}</h3><p>{showFavorites ? 'Tap the heart on a product to save it for later.' : 'Wait For The API To Respond....'}</p><button onClick={() => { setProductPage(0); setQuery(''); setCategory('All'); setShowFavorites(false); }}>Show everything</button></div>}
      </main>
      <section className="quote-banner"><span>✳</span><p>A home is made in the little moments.<br /><em>Make room for more of them.</em></p><small>THOUGHTFULLY CHOSEN FOR YOUR EVERYDAY</small></section>
    </>}
    {view === 'confirmation' && confirmedOrder && <main className="subpage confirmation-page"><div className="confirmation-card"><span className="confirmation-check"><Check size={26} /></span><p className="eyebrow">ORDER CONFIRMED</p><h1>Thank you for your <em>order.</em></h1><p className="subpage-lead">Order #{String(confirmedOrder.id).padStart(5, '0')} · {new Date(confirmedOrder.createdAt).toLocaleDateString('en-IN', { dateStyle: 'medium' })}</p><div className="confirmation-lines">{confirmedOrder.items.map((line, index) => <p key={line.productId ?? index}><span>{line.quantity} × {line.productName}</span><strong>{formatPrice(Number(line.unitPrice) * line.quantity)}</strong></p>)}</div><div className="confirmation-total"><span>Items subtotal</span><strong>{formatPrice(confirmedOrder.subtotal ?? confirmedOrder.total)}</strong></div>{confirmedOrder.shippingFee != null && <div className="confirmation-total"><span>Delivery</span><strong>{formatPrice(confirmedOrder.shippingFee)}</strong></div>}<div className="confirmation-total confirmation-grand"><span>Order total</span><strong>{formatPrice(confirmedOrder.total)}</strong></div><p className="confirmation-payment">{confirmedOrder.paymentMethod === 'RAZORPAY_TEST' ? 'Razorpay test payment captured. No live money was charged.' : 'Cash on delivery. No online payment was made.'}</p><div className="confirmation-actions"><button className="dark-button" onClick={() => setView('orders')}>Track your order <ArrowRight size={16} /></button>    <button className="text-button" onClick={() => { setProductPage(0); setView('shop'); setConfirmedOrder(null); }}>Continue shopping</button></div></div></main>}
    {view === 'orders' && <main className="subpage orders-page"><div className="orders-heading"><div><p className="eyebrow">YOUR ACCOUNT</p><h1>Your <em>orders.</em></h1><p className="subpage-lead">A little record of the good things coming your way.</p></div><button className="order-refresh" onClick={loadOrders} disabled={ordersState === 'loading'}>{ordersState === 'loading' ? <LoaderCircle className="spin" size={15} /> : <RefreshCw size={15} />} {ordersState === 'loading' ? 'Refreshing…' : 'Refresh orders'}</button></div>
      {ordersState === 'error' && orders.length > 0 && <p className="orders-error" role="status">Couldn’t refresh orders. Showing the last loaded results.</p>}
      {(ordersState === 'idle' || ordersState === 'loading') && !orders.length ? <div className="order-message" role="status"><LoaderCircle className="spin" size={22} /><span>Loading your orders…</span></div> :
       ordersState === 'error' && !orders.length ? <div className="empty-state"><h3>We couldn’t load your orders.</h3><p>Check your connection and try again.</p><button onClick={loadOrders}>Try again</button></div> :
       !orders.length ? <div className="empty-state"><h3>No orders yet.</h3><p>When you find something you love, it will appear here.</p><button onClick={() => setView('shop')}>Browse the collection</button></div> :
      <div className="orders-list">{orders.map(order => {
        const items = Array.isArray(order.items) ? order.items : [];
        const tracking = orderTracking(order);
        const legacyOrder = order.subtotal == null && order.shippingFee == null;
        return <article className="order-card" key={order.id}><div className="order-head"><div><span className="product-category">ORDER NO. {String(order.id).padStart(5, '0')}</span><p>{order.createdAt ? new Date(order.createdAt).toLocaleDateString('en-IN', { dateStyle: 'medium' }) : 'Date not recorded'}</p></div><span className={`status-pill status-${String(order.status || 'unknown').toLowerCase()}`}>{order.status === 'CANCELLED' ? <X size={14} /> : <Check size={14} />} {formatStatus(order.status)}</span></div><div className="order-lines">{items.map((line, index) => <p key={line.productId ?? index}>{line.quantity} × {line.productName}<span>{formatPrice(Number(line.unitPrice) * line.quantity)}</span></p>)}</div>
        <div className="order-breakdown"><span>Items subtotal <b>{formatPrice(order.subtotal ?? items.reduce((sum, line) => sum + Number(line.unitPrice) * line.quantity, 0))}</b></span>{order.shippingFee != null && <span>Delivery <b>{formatPrice(order.shippingFee)}</b></span>}<span>Total <b>{formatPrice(order.total)}</b></span></div>
        <p className="order-payment">Payment: {order.paymentMethod === 'COD' ? 'Cash on delivery' : order.paymentMethod === 'RAZORPAY_TEST' ? 'Razorpay test mode' : order.paymentMethod === 'UPI_DEMO' ? 'Simulated UPI (demo)' : order.paymentMethod === 'CARD_DEMO' ? 'Simulated card (demo)' : 'Not recorded'}{order.paymentStatus ? ` · ${formatStatus(order.paymentStatus)}` : ''}</p>
        <div className="order-tracking"><h3>Order tracking</h3><ol>{tracking.map(step => <li className={`${step.complete ? 'complete' : ''}${step.status === 'CANCELLED' ? ' tracking-cancelled' : ''}`} key={step.status}><span className="tracking-dot" /><span>{formatStatus(step.status)}</span></li>)}</ol></div>
        <div className="order-delivery"><span>Delivering to</span><strong>{order.shippingAddress || 'Address not recorded'}{order.pinCode ? ` · ${order.pinCode}` : ''}</strong></div>
        {legacyOrder && <p className="order-legacy-note">This order was placed before checkout details were added. Delivery and payment details were not recorded.</p>}
      </article>;
      })}</div>}
      <PaginationControls pageInfo={ordersPageInfo} onPageChange={setOrdersPage} label="Orders" />
    </main>}
    {view === 'manage' && user?.role === 'ADMIN' && <main className="subpage manage-page"><p className="eyebrow">THE BACK OFFICE</p><div className="manage-heading"><div><h1>Manage the <em>shop.</em></h1><p className="subpage-lead">Keep the collection thoughtful and in stock.</p></div><button className="dark-button" onClick={() => setProductForm({ name: '', description: '', category: '', price: '', stock: 0, imageUrl: fallbackImage })}><Plus size={16} /> Add a product</button></div>
      <div className="manage-table"><div className="table-head"><span>PRODUCT</span><span>CATEGORY</span><span>PRICE (INR)</span><span>STOCK</span><span>ACTIONS</span></div>{products.map(p => <div className="table-row" key={p.id}><div className="table-product"><img src={productImage(p)} onError={handleImageError} alt="" /><strong>{p.name}</strong></div><span>{p.category}</span><span>{formatPrice(p.price)}</span><span>{p.stock}</span><div className="table-actions"><button onClick={() => setProductForm({ ...p })}>Edit</button><button className="delete-action" onClick={() => removeProduct(p)} aria-label={`Delete ${p.name}`}><Trash2 size={16} /></button></div></div>)}</div>
      <PaginationControls pageInfo={productPageInfo} onPageChange={setProductPage} label="Products" />
      <section className="inventory-section"><p className="eyebrow">INVENTORY HEALTH</p><h2>Stock watch</h2>{lowStockProducts.length ? <div className="low-stock-list">{lowStockProducts.map(product => <span key={product.id}><strong>{product.name}</strong>{product.stock === 0 ? ' · Out of stock' : ` · Only ${product.stock} left`}</span>)}</div> : <p className="subpage-lead">All products have more than five units in stock.</p>}
        <form className="inventory-adjust-form" onSubmit={adjustInventory}><label className="field-label">PRODUCT<select required value={inventoryProductId} onChange={e => setInventoryProductId(e.target.value)}><option value="">Select a product</option>{inventoryProducts.map(product => <option key={product.id} value={product.id}>{product.name} · stock {product.stock}</option>)}</select></label><label className="field-label">QUANTITY CHANGE<input type="number" required min="-100000" max="100000" step="1" value={inventoryAdjustment.quantityChange} onChange={e => setInventoryAdjustment(current => ({ ...current, quantityChange: e.target.value }))} placeholder="e.g. 10 or -2" /></label><label className="field-label">REASON<input required maxLength="200" value={inventoryAdjustment.reason} onChange={e => setInventoryAdjustment(current => ({ ...current, reason: e.target.value }))} placeholder="Restock, damage, count correction…" /></label><button className="dark-button" disabled={busy || !inventoryProductId}>{busy ? 'Saving…' : 'Adjust stock'}</button></form>
        <h3 className="inventory-history-title">Recent stock changes</h3>{!inventoryMovements.length ? <p className="subpage-lead">Inventory changes will be listed here.</p> : <div className="inventory-history">{inventoryMovements.map(movement => <article key={movement.id}><div><strong>{movement.productName}</strong><span>{movement.reason} · {movement.actor}</span></div><b className={movement.quantityChange > 0 ? 'stock-increase' : 'stock-decrease'}>{movement.quantityChange > 0 ? '+' : ''}{movement.quantityChange}</b><span>{movement.stockAfter} in stock</span><time>{new Date(movement.createdAt).toLocaleDateString('en-IN')}</time></article>)}</div>}
      </section>
      <section className="admin-orders"><p className="eyebrow">FULFILMENT</p><h2>Customer orders</h2>{!adminOrders.length ? <p className="subpage-lead">There are no orders to fulfil yet.</p> : <div className="admin-order-list">{adminOrders.map(order => {
        const currentStep = orderFlow.indexOf(order.status);
        const nextStatus = currentStep >= 0 ? orderFlow[currentStep + 1] : null;
        return <article className="admin-order" key={order.id}><div><strong>Order #{String(order.id).padStart(5, '0')}</strong><span>{order.items.map(item => `${item.quantity} × ${item.productName}`).join(', ')}</span><span>{order.pinCode ? `PIN ${order.pinCode} · ` : ''}{formatPrice(order.total)} · {order.status.replaceAll('_', ' ')}</span></div><div className="admin-order-actions">
          {nextStatus && <button className="dark-button" onClick={() => updateOrderStatus(order, nextStatus)}>Mark {nextStatus.replaceAll('_', ' ').toLowerCase()}</button>}
          {['PLACED', 'CONFIRMED'].includes(order.status) && <button className="cancel-order" onClick={() => updateOrderStatus(order, 'CANCELLED')}>Cancel order</button>}
        </div></article>;
      })}</div>}<PaginationControls pageInfo={adminOrdersPageInfo} onPageChange={setAdminOrdersPage} label="Customer orders" /></section>
    </main>}
    <footer className="footer"><div><a className="brand footer-brand" href="#" onClick={e => { e.preventDefault(); setView('shop'); }}>E-<span>Cart</span><i>.</i></a><p>Thoughtful goods for homes across India.</p></div><div className="footer-links"><span>MADE FOR EVERYDAY</span><p>Prices shown in INR.<br />Delivering across India.</p></div><div className="footer-bottom"><span>© 2026 E-CART INDIA</span><span>Developed by <strong>Naveen Kumar</strong></span><span>GOOD THINGS, GENTLY MADE.</span></div></footer>

    {notice && <div className="toast" role="status">{notice}<button onClick={() => setNotice('')} aria-label="Dismiss"><X size={16} /></button></div>}
    {drawer && <div className="overlay" onMouseDown={e => { if (e.target === e.currentTarget) setDrawer(''); }}>
      {drawer === 'cart' && <aside className="side-panel"><div className="panel-head"><div><p className="eyebrow">YOUR SELECTION</p><h2>Your bag <span>({count})</span></h2></div><button className="icon-button" onClick={() => setDrawer('')}><X /></button></div>
        {!token ? <div className="empty-state"><ShoppingBag /><h3>Your bag is waiting.</h3><p>Sign in to keep your picks safe.</p><button onClick={() => { setAuthMode('login'); setDrawer('auth'); }}>Sign in</button></div> : !cart.items.length ? <div className="empty-state"><ShoppingBag /><h3>A little room for something good.</h3><p>Your bag is empty for now.</p><button onClick={() => { setDrawer(''); setView('shop'); }}>Explore the collection</button></div> :
        <><div className="cart-lines">{cart.items.map(item => <div className="cart-line" key={item.productId}><img src={productImage(item)} onError={handleImageError} alt={item.name} /><div className="cart-line-info"><span className="product-category">THOUGHTFULLY PICKED</span><h3>{item.name}</h3><b>{formatPrice(item.unitPrice)}</b><div className="quantity"><button onClick={() => changeQuantity(item, item.quantity - 1)} aria-label="Decrease quantity"><Minus size={14} /></button><span>{item.quantity}</span><button disabled={item.quantity >= item.stock} onClick={() => changeQuantity(item, item.quantity + 1)} aria-label="Increase quantity"><Plus size={14} /></button></div></div><button className="remove-item" onClick={() => changeQuantity(item, 0)} aria-label={`Remove ${item.name}`}><Trash2 size={16} /></button></div>)}</div>
          <form className="checkout-box" onSubmit={checkout}>
            {addresses.length > 0 && <div className="saved-addresses"><label className="field-label">SAVED DELIVERY ADDRESSES<select value={selectedAddressId} onChange={e => selectAddress(addresses.find(item => String(item.id) === e.target.value) || null)}><option value="">Enter a new address</option>{addresses.map(saved => <option key={saved.id} value={saved.id}>{saved.label}{saved.defaultAddress ? ' · Default' : ''} — {saved.address}, {saved.pinCode}</option>)}</select></label><div className="saved-address-actions">{addresses.map(saved => <span key={saved.id}>{saved.defaultAddress && <b>Default</b>}{!saved.defaultAddress && <button type="button" onClick={() => makeDefaultAddress(saved)}>Make default</button>}<button type="button" className="delete-address" onClick={() => deleteAddress(saved)} aria-label={`Delete ${saved.label} address`}><Trash2 size={13} /></button></span>)}</div></div>}
            {addressesState === 'error' && <p className="address-error">Saved addresses could not be loaded. You can still enter your address below.</p>}
            <label className="field-label">DELIVERY ADDRESS IN INDIA<textarea required maxLength="500" value={address} onChange={e => { setSelectedAddressId(''); setAddress(e.target.value); }} placeholder="Flat / house no., street, area, city, state" /></label>
            <label className="field-label">6-DIGIT PIN CODE<input required inputMode="numeric" autoComplete="postal-code" pattern="[1-9][0-9]{5}" maxLength="6" value={pinCode} onChange={e => { setSelectedAddressId(''); setPinCode(e.target.value.replace(/\D/g, '').slice(0, 6)); }} placeholder="e.g. 560001" /></label>
            <label className="save-address-option"><input type="checkbox" checked={saveAddress} onChange={e => setSaveAddress(e.target.checked)} /> Save this address for next time</label>
            <label className="field-label">PAYMENT METHOD<select value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)}><option value="COD">Cash on delivery</option><option value="UPI_TEST">UPI via Razorpay test checkout</option><option value="CARD_TEST">Card via Razorpay test checkout</option></select></label>
            {paymentMethod !== 'COD' && <p className="payment-test-note">Continue to the secure Razorpay sandbox to try {paymentMethod === 'UPI_TEST' ? 'UPI' : 'card'} payment. Test credentials only; no live charge. If it fails, you can retry without losing your bag.</p>}
            <div className="checkout-totals"><div><span>Items subtotal</span><strong>{formatPrice(cart.total)}</strong></div><div><span>Delivery (flat fee)</span><strong>{formatPrice(99)}</strong></div><div className="checkout-grand-total"><span>Order total</span><strong>{formatPrice(Number(cart.total) + 99)}</strong></div></div>
            <p className="checkout-note">Product prices are GST-inclusive. Delivery is a flat ₹99. PIN format only; serviceability is not checked. Online payment is sandbox-only and does not process live charges.</p>
            <button className="dark-button full-button" disabled={busy}>{busy ? <LoaderCircle className="spin" size={17} /> : <>{paymentMethod === 'COD' ? 'Place COD order' : `Continue to ${paymentMethod === 'UPI_TEST' ? 'UPI' : 'card'} test checkout`} <ArrowRight size={16} /></>}</button></form></>}
      </aside>}
      {drawer === 'auth' && <aside className="auth-panel"><button className="icon-button close-auth" onClick={() => setDrawer('')}><X /></button><p className="eyebrow">{authMode === 'login' ? 'WELCOME BACK' : 'A GOOD PLACE TO START'}</p><h2>{authMode === 'login' ? <>Come on <em>in.</em></> : <>Make yourself <em>at home.</em></>}</h2><p className="auth-intro">{authMode === 'login' ? 'Sign in to see your bag and orders.' : 'Create an account to keep your good things together.'}</p><form onSubmit={submitAuth}>
        {authMode === 'register' && <label className="field-label">YOUR NAME<input required maxLength="120" value={authForm.name} onChange={e => setAuthForm({ ...authForm, name: e.target.value })} placeholder="What should we call you?" /></label>}
        <label className="field-label">EMAIL ADDRESS<input required type="email" value={authForm.email} onChange={e => setAuthForm({ ...authForm, email: e.target.value })} placeholder="you@example.com" /></label>
        <label className="field-label">PASSWORD<input required minLength={authMode === 'register' ? 10 : undefined} type="password" value={authForm.password} onChange={e => setAuthForm({ ...authForm, password: e.target.value })} placeholder={authMode === 'register' ? 'At least 10 characters' : 'Your password'} /></label>
        <button className="dark-button full-button" disabled={busy}>{busy ? <LoaderCircle className="spin" size={17} /> : <>{authMode === 'login' ? 'Sign in' : 'Create account'} <ArrowRight size={16} /></>}</button>
      </form><p className="switch-auth">{authMode === 'login' ? 'New around here?' : 'Already have an account?'} <button onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>{authMode === 'login' ? 'Create an account' : 'Sign in'}</button></p></aside>}
    </div>}
    {activeProduct && <div className="overlay modal-overlay" onMouseDown={e => { if (e.target === e.currentTarget) setActiveProduct(null); }}><section className="product-detail-modal" role="dialog" aria-modal="true" aria-labelledby="product-detail-title">
      <div className="panel-head"><div><p className="eyebrow">{activeProduct.category}</p><h2 id="product-detail-title">{activeProduct.name}</h2></div><button className="icon-button" onClick={() => setActiveProduct(null)} aria-label="Close product details"><X /></button></div>
      <div className="product-detail"><img src={productImage(activeProduct)} onError={handleImageError} alt={activeProduct.name} /><div><p>{activeProduct.description}</p><div className="detail-rating"><span>★</span> {Number(activeProduct.averageRating || 0) > 0 ? Number(activeProduct.averageRating).toFixed(1) : 'New'} <small>({activeProduct.reviewCount || reviews.length} reviews)</small></div><strong>{formatPrice(activeProduct.price)}</strong><p className="detail-stock">{activeProduct.stock > 0 ? `${activeProduct.stock} in stock` : 'Currently sold out'}</p><button className="dark-button" disabled={!activeProduct.stock} onClick={() => addToCart(activeProduct)}><ShoppingBag size={16} /> Add to bag</button></div></div>
      <section className="review-section"><div className="review-heading"><h3>Customer reviews</h3><span>{reviews.length} {reviews.length === 1 ? 'review' : 'reviews'}</span></div>
        {token && user?.role === 'CUSTOMER' && <form className="review-form" onSubmit={submitReview}><label className="field-label">YOUR RATING<select value={reviewForm.rating} onChange={e => setReviewForm({ ...reviewForm, rating: e.target.value })}>{[5, 4, 3, 2, 1].map(rating => <option key={rating} value={rating}>{rating} out of 5</option>)}</select></label><label className="field-label">YOUR REVIEW<textarea required maxLength="1000" value={reviewForm.comment} onChange={e => setReviewForm({ ...reviewForm, comment: e.target.value })} placeholder="Share what you thought about this product." /></label><button className="dark-button" disabled={busy}>Submit review</button><small>Reviews are limited to verified purchasers and one per customer.</small></form>}
        {!token && <p className="review-hint">Sign in with a customer account to leave a review after purchase.</p>}
        {!reviews.length ? <p className="review-hint">No reviews yet.</p> : <div className="review-list">{reviews.map(review => <article className="review-card" key={review.id}><div><strong>{review.customerName}</strong><span>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span></div><p>{review.comment}</p><small>{new Date(review.createdAt).toLocaleDateString('en-IN')}</small></article>)}</div>}
      </section>
    </section></div>}
    {productForm && <div className="overlay modal-overlay" onMouseDown={e => { if (e.target === e.currentTarget) setProductForm(null); }}><div className="product-modal"><div className="panel-head"><div><p className="eyebrow">THE COLLECTION</p><h2>{productForm.id ? 'Edit product' : 'Add a product'}</h2></div><button className="icon-button" onClick={() => setProductForm(null)}><X /></button></div><form onSubmit={persistProduct} className="product-editor">
      {[['name','PRODUCT NAME','text'],['category','CATEGORY','text'],['price','PRICE (₹ INR)','number'],['stock','STOCK','number']].map(([key,label,type]) => <label className="field-label" key={key}>{label}<input required type={type} min={type === 'number' ? (key === 'price' ? '0.01' : '0') : undefined} step={key === 'price' ? '0.01' : undefined} value={productForm[key]} onChange={e => setProductForm({ ...productForm, [key]: e.target.value })} /></label>)}
      <label className="field-label wide-field">DESCRIPTION<textarea required maxLength="2000" value={productForm.description} onChange={e => setProductForm({ ...productForm, description: e.target.value })} /></label>
      <label className="field-label wide-field">IMAGE URL<input value={productForm.imageUrl || ''} onChange={e => setProductForm({ ...productForm, imageUrl: e.target.value })} placeholder="https://example.com/product-photo.jpg" /></label>
      <button className="dark-button full-button wide-field" disabled={busy}>{busy ? 'Saving…' : 'Save product'} <ArrowRight size={16} /></button>
    </form></div></div>}
  </div>;
}

createRoot(document.getElementById('root')).render(<App />);
