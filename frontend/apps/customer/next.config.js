/** @type {import('next').NextConfig} */
const nextConfig = {
  transpilePackages: ['@astha/ui', '@astha/i18n', '@astha/mocks', '@astha/api-client'],
  reactStrictMode: true,
};

module.exports = nextConfig;
