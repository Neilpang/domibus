export class User {
  id: number;
  username: string;
  authorities: Array<string>;

  constructor(id: number, login: string, profile: string, authorities: Array<string>) {
  }
}
