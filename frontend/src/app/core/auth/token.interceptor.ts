import {HttpInterceptorFn} from '@angular/common/http';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. Get the token from localStorage
  const token = localStorage.getItem('access_token');
  let authReq = req;

  // 2. If token exists, clone the request and add the Bearer header
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 3. Pass the cloned request instead of the original
  return next(authReq);
};
