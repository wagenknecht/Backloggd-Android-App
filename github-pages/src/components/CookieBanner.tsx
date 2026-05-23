import { useState, useEffect } from 'react';
import { useI18n } from '@/lib/i18n';

export const CookieBanner = () => {
  const [isVisible, setIsVisible] = useState(false);
  const { t } = useI18n();

  useEffect(() => {
    const consent = localStorage.getItem('cookieConsent');
    if (consent === null) {
      setIsVisible(true);
    } else if (consent === 'true') {
      loadGoogleAnalytics();
    }
  }, []);

  const loadGoogleAnalytics = () => {
    const script = document.createElement('script');
    script.async = true;
    script.src = 'https://www.googletagmanager.com/gtag/js?id=G-3NL24CLCQ5';
    document.head.appendChild(script);

    window.dataLayer = window.dataLayer || [];
    function gtag(...args: any[]) {
      window.dataLayer.push(args);
    }
    gtag('js', new Date());
    gtag('config', 'G-3NL24CLCQ5');
  };

  const handleAccept = () => {
    localStorage.setItem('cookieConsent', 'true');
    setIsVisible(false);
    loadGoogleAnalytics();
  };

  const handleDecline = () => {
    localStorage.setItem('cookieConsent', 'false');
    setIsVisible(false);
  };

  if (!isVisible) return null;

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-gray-900 text-white p-4 shadow-lg z-50 flex flex-col sm:flex-row items-center justify-between gap-4 border-t border-gray-700">
      <div className="text-sm text-center sm:text-left">
        <p>
          {t('cookie.text')}
        </p>
      </div>
      <div className="flex gap-3">
        <button
          onClick={handleDecline}
          className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white transition-colors border border-gray-600 rounded hover:bg-gray-800"
        >
          {t('cookie.decline')}
        </button>
        <button
          onClick={handleAccept}
          className="px-4 py-2 text-sm font-medium bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors shadow-sm"
        >
          {t('cookie.accept')}
        </button>
      </div>
    </div>
  );
};

// Add type definition for window.dataLayer
declare global {
  interface Window {
    dataLayer: any[];
  }
}
